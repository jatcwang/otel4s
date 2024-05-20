/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.sdk.testkit.metrics

import cats.Foldable
import cats.Monad
import cats.effect.Concurrent
import cats.effect.std.Queue
import cats.syntax.foldable._
import cats.syntax.functor._
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationSelector
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationTemporalitySelector
import org.typelevel.otel4s.sdk.metrics.exporter.CardinalityLimitSelector
import org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter

final class InMemoryMetricExporter[F[_]: Monad] private (
    queue: Queue[F, MetricData],
    val aggregationTemporalitySelector: AggregationTemporalitySelector,
    val defaultAggregationSelector: AggregationSelector,
    val defaultCardinalityLimitSelector: CardinalityLimitSelector
) extends MetricExporter.Push[F] {

  def name: String = "InMemoryMetricExporter"

  def exportMetrics[G[_]: Foldable](metrics: G[MetricData]): F[Unit] =
    metrics.traverse_(metric => queue.offer(metric))

  def flush: F[Unit] =
    Monad[F].unit

  def exportedMetrics: F[List[MetricData]] =
    queue.tryTakeN(None)

  def reset: F[Unit] =
    queue.tryTakeN(None).void
}

object InMemoryMetricExporter {

  /** Creates a `MetricExporter` that keeps metrics in-memory.
    *
    * @param capacity
    *   the capacity of the internal queue
    *
    * @param aggregationTemporalitySelector
    *   the preferred aggregation for the given instrument type
    *
    * @param defaultAggregationSelector
    *   the preferred aggregation for the given instrument type. If no views are
    *   configured for a metric instrument, an aggregation provided by the
    *   selector will be used.
    *
    * @param defaultCardinalityLimitSelector
    *   the preferred cardinality limit for the given instrument type. If no
    *   views are configured for a metric instrument, an aggregation provided by
    *   the selector will be used.
    */
  def create[F[_]: Concurrent](
      capacity: Option[Int],
      aggregationTemporalitySelector: AggregationTemporalitySelector =
        AggregationTemporalitySelector.alwaysCumulative,
      defaultAggregationSelector: AggregationSelector =
        AggregationSelector.default,
      defaultCardinalityLimitSelector: CardinalityLimitSelector =
        CardinalityLimitSelector.default
  ): F[InMemoryMetricExporter[F]] =
    for {
      queue <- capacity.fold(Queue.unbounded[F, MetricData])(Queue.bounded(_))
    } yield new InMemoryMetricExporter[F](
      queue,
      aggregationTemporalitySelector,
      defaultAggregationSelector,
      defaultCardinalityLimitSelector
    )

}
