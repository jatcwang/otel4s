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

package org.typelevel.otel4s.sdk.metrics.exporter

import cats.Applicative
import cats.Foldable
import org.typelevel.otel4s.sdk.metrics.data.MetricData

/** `MetricExporter` is a push based interface for exporting `MetricData`.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/metrics/sdk/#metricexporter]]
  */
trait MetricExporter[F[_]] {

  /** The name of the exporter.
    */
  def name: String

  /** The preferred aggregation temporality for the given instrument.
    */
  def aggregationTemporalitySelector: AggregationTemporalitySelector

  /** The preferred aggregation for the given instrument.
    */
  def defaultAggregationSelector: AggregationSelector

  /** The preferred cardinality limit for the given instrument.
    */
  def defaultCardinalityLimitSelector: CardinalityLimitSelector

  /** Exports the sampled `MetricData`.
    *
    * @param metrics
    *   the sampled metrics to export
    */
  def exportMetrics[G[_]: Foldable](metrics: G[MetricData]): F[Unit]

  /** Exports the collection of sampled `MetricData` that have not yet been
    * exported.
    */
  def flush: F[Unit]

  override def toString: String =
    name

}

object MetricExporter {

  /** Creates a no-op implementation of the [[MetricExporter]].
    *
    * All export operations are no-op.
    */
  def noop[F[_]: Applicative]: MetricExporter[F] =
    new Noop

  private final class Noop[F[_]: Applicative] extends MetricExporter[F] {
    val name: String = "MetricExporter.Noop"

    def aggregationTemporalitySelector: AggregationTemporalitySelector =
      AggregationTemporalitySelector.alwaysCumulative

    def defaultAggregationSelector: AggregationSelector =
      AggregationSelector.default

    def defaultCardinalityLimitSelector: CardinalityLimitSelector =
      CardinalityLimitSelector.default

    def exportMetrics[G[_]: Foldable](metrics: G[MetricData]): F[Unit] =
      Applicative[F].unit

    def flush: F[Unit] =
      Applicative[F].unit
  }

}
