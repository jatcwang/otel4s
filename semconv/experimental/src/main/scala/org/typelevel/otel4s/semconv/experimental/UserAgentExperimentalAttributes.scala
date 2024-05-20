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

package org.typelevel.otel4s.semconv.experimental.attributes

import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.AttributeKey._

// DO NOT EDIT, this is an Auto-generated file from buildscripts/semantic-convention/templates/SemanticAttributes.scala.j2
object UserAgentExperimentalAttributes {

  /** Name of the user-agent extracted from original. Usually refers to the
    * browser's name.
    *
    * @note
    *   - <a href="https://www.whatsmyua.info">Example</a> of extracting
    *     browser's name from original string. In the case of using a user-agent
    *     for non-browser products, such as microservices with multiple
    *     names/versions inside the `user_agent.original`, the most significant
    *     name SHOULD be selected. In such a scenario it should align with
    *     `user_agent.version`
    */
  val UserAgentName: AttributeKey[String] = string("user_agent.name")

  /** Value of the <a
    * href="https://www.rfc-editor.org/rfc/rfc9110.html#field.user-agent">HTTP
    * User-Agent</a> header sent by the client.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.UserAgentAttributes.UserAgentOriginal` instead.",
    "0.5.0"
  )
  val UserAgentOriginal: AttributeKey[String] = string("user_agent.original")

  /** Version of the user-agent extracted from original. Usually refers to the
    * browser's version
    *
    * @note
    *   - <a href="https://www.whatsmyua.info">Example</a> of extracting
    *     browser's version from original string. In the case of using a
    *     user-agent for non-browser products, such as microservices with
    *     multiple names/versions inside the `user_agent.original`, the most
    *     significant version SHOULD be selected. In such a scenario it should
    *     align with `user_agent.name`
    */
  val UserAgentVersion: AttributeKey[String] = string("user_agent.version")

}
