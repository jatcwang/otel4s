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

package org.typelevel.otel4s.sdk

import cats.kernel.laws.discipline.HashTests
import cats.kernel.laws.discipline.MonoidTests
import munit.DisciplineSuite
import org.typelevel.otel4s.sdk.arbitrary._

class AttributesLawTests extends DisciplineSuite {

  checkAll("Attributes.HashLaws", HashTests[Attributes].hash)
  checkAll("Attributes.MonoidLaws", MonoidTests[Attributes].monoid)

}