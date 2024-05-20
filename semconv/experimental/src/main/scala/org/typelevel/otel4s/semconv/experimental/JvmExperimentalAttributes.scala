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
object JvmExperimentalAttributes {

  /** Name of the buffer pool.
    *
    * @note
    *   - Pool names are generally obtained via <a
    *     href="https://docs.oracle.com/en/java/javase/11/docs/api/java.management/java/lang/management/BufferPoolMXBean.html#getName()">BufferPoolMXBean#getName()</a>.
    */
  val JvmBufferPoolName: AttributeKey[String] = string("jvm.buffer.pool.name")

  /** Name of the garbage collector action.
    *
    * @note
    *   - Garbage collector action is generally obtained via <a
    *     href="https://docs.oracle.com/en/java/javase/11/docs/api/jdk.management/com/sun/management/GarbageCollectionNotificationInfo.html#getGcAction()">GarbageCollectionNotificationInfo#getGcAction()</a>.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.JvmAttributes.JvmGcAction` instead.",
    "0.5.0"
  )
  val JvmGcAction: AttributeKey[String] = string("jvm.gc.action")

  /** Name of the garbage collector.
    *
    * @note
    *   - Garbage collector name is generally obtained via <a
    *     href="https://docs.oracle.com/en/java/javase/11/docs/api/jdk.management/com/sun/management/GarbageCollectionNotificationInfo.html#getGcName()">GarbageCollectionNotificationInfo#getGcName()</a>.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.JvmAttributes.JvmGcName` instead.",
    "0.5.0"
  )
  val JvmGcName: AttributeKey[String] = string("jvm.gc.name")

  /** Name of the memory pool.
    *
    * @note
    *   - Pool names are generally obtained via <a
    *     href="https://docs.oracle.com/en/java/javase/11/docs/api/java.management/java/lang/management/MemoryPoolMXBean.html#getName()">MemoryPoolMXBean#getName()</a>.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.JvmAttributes.JvmMemoryPoolName` instead.",
    "0.5.0"
  )
  val JvmMemoryPoolName: AttributeKey[String] = string("jvm.memory.pool.name")

  /** The type of memory.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.JvmAttributes.JvmMemoryType` instead.",
    "0.5.0"
  )
  val JvmMemoryType: AttributeKey[String] = string("jvm.memory.type")

  /** Whether the thread is daemon or not.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.JvmAttributes.JvmThreadDaemon` instead.",
    "0.5.0"
  )
  val JvmThreadDaemon: AttributeKey[Boolean] = boolean("jvm.thread.daemon")

  /** State of the thread.
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.JvmAttributes.JvmThreadState` instead.",
    "0.5.0"
  )
  val JvmThreadState: AttributeKey[String] = string("jvm.thread.state")
  // Enum definitions

  /** Values for [[JvmMemoryType]].
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.JvmAttributes.JvmMemoryTypeValue` instead.",
    "0.5.0"
  )
  abstract class JvmMemoryTypeValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object JvmMemoryTypeValue {

    /** Heap memory. */
    case object Heap extends JvmMemoryTypeValue("heap")

    /** Non-heap memory. */
    case object NonHeap extends JvmMemoryTypeValue("non_heap")
  }

  /** Values for [[JvmThreadState]].
    */
  @deprecated(
    "use `org.typelevel.otel4s.semconv.attributes.JvmAttributes.JvmThreadStateValue` instead.",
    "0.5.0"
  )
  abstract class JvmThreadStateValue(val value: String)
  @annotation.nowarn("cat=deprecation")
  object JvmThreadStateValue {

    /** A thread that has not yet started is in this state. */
    case object New extends JvmThreadStateValue("new")

    /** A thread executing in the Java virtual machine is in this state. */
    case object Runnable extends JvmThreadStateValue("runnable")

    /** A thread that is blocked waiting for a monitor lock is in this state. */
    case object Blocked extends JvmThreadStateValue("blocked")

    /** A thread that is waiting indefinitely for another thread to perform a
      * particular action is in this state.
      */
    case object Waiting extends JvmThreadStateValue("waiting")

    /** A thread that is waiting for another thread to perform an action for up
      * to a specified waiting time is in this state.
      */
    case object TimedWaiting extends JvmThreadStateValue("timed_waiting")

    /** A thread that has exited is in this state. */
    case object Terminated extends JvmThreadStateValue("terminated")
  }

}
