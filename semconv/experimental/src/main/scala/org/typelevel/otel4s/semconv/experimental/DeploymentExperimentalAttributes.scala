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
object DeploymentExperimentalAttributes {

  /** Name of the <a
    * href="https://wikipedia.org/wiki/Deployment_environment">deployment
    * environment</a> (aka deployment tier).
    *
    * @note
    *   - `deployment.environment` does not affect the uniqueness constraints
    *     defined through the `service.namespace`, `service.name` and
    *     `service.instance.id` resource attributes. This implies that resources
    *     carrying the following attribute combinations MUST be considered to be
    *     identifying the same service:<li>`service.name=frontend`,
    *     `deployment.environment=production`</li> <li>`service.name=frontend`,
    *     `deployment.environment=staging`.</li>
    */
  val DeploymentEnvironment: AttributeKey[String] = string(
    "deployment.environment"
  )

}
