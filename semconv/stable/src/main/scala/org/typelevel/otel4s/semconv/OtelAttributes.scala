/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.semconv.attributes

import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.AttributeKey._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/semantic-convention/templates/SemanticAttributes.scala.j2
object OtelAttributes {

  /** The name of the instrumentation scope - (`InstrumentationScope.Name` in
    * OTLP).
    */
  val OtelScopeName: AttributeKey[String] = string("otel.scope.name")

  /** The version of the instrumentation scope - (`InstrumentationScope.Version`
    * in OTLP).
    */
  val OtelScopeVersion: AttributeKey[String] = string("otel.scope.version")

  /** Name of the code, either &quot;OK&quot; or &quot;ERROR&quot;. MUST NOT be
    * set if the status code is UNSET.
    */
  val OtelStatusCode: AttributeKey[String] = string("otel.status_code")

  /** Description of the Status if it has a value, otherwise not set.
    */
  val OtelStatusDescription: AttributeKey[String] = string(
    "otel.status_description"
  )
  // Enum definitions

  /** Values for [[OtelStatusCode]].
    */
  abstract class OtelStatusCodeValue(val value: String)
  object OtelStatusCodeValue {

    /** The operation has been validated by an Application developer or Operator
      * to have completed successfully.
      */
    case object Ok extends OtelStatusCodeValue("OK")

    /** The operation contains an error. */
    case object Error extends OtelStatusCodeValue("ERROR")
  }

}
