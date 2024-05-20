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

package org.typelevel.otel4s.sdk.metrics
package exporter

/** Used by the `MetricReader` to decide the default aggregation.
  */
trait CardinalityLimitSelector {

  /** Returns preferred cardinality limit for the given [[InstrumentType]].
    */
  def select(instrumentType: InstrumentType): Int
}

object CardinalityLimitSelector {

  private object Defaults {
    // see https://opentelemetry.io/docs/specs/otel/metrics/sdk/#cardinality-limits
    val CardinalityLimit: Int = 2000
  }

  /** Returns default cardinality limit (2000) for all instruments.
    */
  def default: CardinalityLimitSelector = _ => Defaults.CardinalityLimit

}
