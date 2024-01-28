/*
 * Copyright 2022 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typelevel.otel4s
package oteljava
package metrics

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.effect.std.Dispatcher
import cats.syntax.all._
import io.opentelemetry.api.metrics.{Meter => JMeter}
import io.opentelemetry.api.metrics.{
  ObservableMeasurement => JObservableMeasurement
}
import io.opentelemetry.api.metrics.DoubleGaugeBuilder
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement
import io.opentelemetry.api.metrics.ObservableLongMeasurement
import org.typelevel.otel4s.metrics._

private[oteljava] case class ObservableGaugeBuilderImpl[F[_], A](
    factory: ObservableGaugeBuilderImpl.Factory[F, A],
    name: String,
    unit: Option[String] = None,
    description: Option[String] = None
) extends ObservableGauge.Builder[F, A] {

  def withUnit(unit: String): ObservableGauge.Builder[F, A] =
    copy(unit = Option(unit))

  def withDescription(description: String): ObservableGauge.Builder[F, A] =
    copy(description = Option(description))

  def createWithCallback(
      cb: ObservableMeasurement[F, A] => F[Unit]
  ): Resource[F, ObservableGauge] =
    factory.createWithCallback(name, unit, description, cb)

  def create(
      measurements: F[Iterable[Measurement[A]]]
  ): Resource[F, ObservableGauge] =
    factory.create(name, unit, description, measurements)
}

private[oteljava] object ObservableGaugeBuilderImpl {

  def apply[F[_]: Async, A: MeasurementValue](
      jMeter: JMeter,
      name: String
  ): ObservableGauge.Builder[F, A] =
    MeasurementValue[A] match {
      case MeasurementValue.LongMeasurementValue(cast) =>
        ObservableGaugeBuilderImpl(longFactory(jMeter, cast), name)

      case MeasurementValue.DoubleMeasurementValue(cast) =>
        ObservableGaugeBuilderImpl(doubleFactory(jMeter, cast), name)
    }

  private[oteljava] sealed abstract class Factory[F[_]: Async, A](
      jMeter: JMeter
  ) {
    type JMeasurement <: JObservableMeasurement

    final def create(
        name: String,
        unit: Option[String],
        description: Option[String],
        measurements: F[Iterable[Measurement[A]]]
    ): Resource[F, ObservableGauge] =
      createInternal(name, unit, description) { om =>
        measurements.flatMap { ms =>
          Async[F].delay(
            ms.foreach(m => doRecord(om, m.value, m.attributes))
          )
        }
      }

    final def createWithCallback(
        name: String,
        unit: Option[String],
        description: Option[String],
        cb: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] =
      createInternal(name, unit, description) { om =>
        cb(
          new ObservableMeasurement[F, A] {
            def record(value: A, attributes: Attributes): F[Unit] =
              Async[F].delay(
                doRecord(om, value, attributes)
              )
          }
        )
      }

    protected def create(
        builder: DoubleGaugeBuilder,
        dispatcher: Dispatcher[F],
        cb: JMeasurement => F[Unit]
    ): AutoCloseable

    protected def doRecord(
        measurement: JMeasurement,
        value: A,
        attributes: Attributes
    ): Unit

    private final def createInternal(
        name: String,
        unit: Option[String],
        description: Option[String]
    )(cb: JMeasurement => F[Unit]): Resource[F, ObservableGauge] =
      Dispatcher.sequential.flatMap { dispatcher =>
        Resource
          .fromAutoCloseable(Async[F].delay {
            val b = jMeter.gaugeBuilder(name)
            unit.foreach(b.setUnit)
            description.foreach(b.setDescription)
            create(b, dispatcher, cb)
          })
          .as(new ObservableGauge {})
      }

  }

  private def longFactory[F[_]: Async, A](
      jMeter: JMeter,
      cast: A => Long
  ): Factory[F, A] =
    new Factory[F, A](jMeter) {
      type JMeasurement = ObservableLongMeasurement

      protected def create(
          builder: DoubleGaugeBuilder,
          dispatcher: Dispatcher[F],
          cb: ObservableLongMeasurement => F[Unit]
      ): AutoCloseable =
        builder
          .ofLongs()
          .buildWithCallback(om => dispatcher.unsafeRunSync(cb(om)))

      protected def doRecord(
          om: ObservableLongMeasurement,
          value: A,
          attributes: Attributes
      ): Unit =
        om.record(cast(value), Conversions.toJAttributes(attributes))
    }

  private def doubleFactory[F[_]: Async, A](
      jMeter: JMeter,
      cast: A => Double
  ): Factory[F, A] =
    new Factory[F, A](jMeter) {
      type JMeasurement = ObservableDoubleMeasurement

      protected def create(
          builder: DoubleGaugeBuilder,
          dispatcher: Dispatcher[F],
          cb: ObservableDoubleMeasurement => F[Unit]
      ): AutoCloseable =
        builder.buildWithCallback(om => dispatcher.unsafeRunSync(cb(om)))

      protected def doRecord(
          om: ObservableDoubleMeasurement,
          value: A,
          attributes: Attributes
      ): Unit =
        om.record(cast(value), Conversions.toJAttributes(attributes))
    }

}