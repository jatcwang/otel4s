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
object JvmAttributes {

  /** Name of the garbage collector action.
    *
    * @note
    *   - Garbage collector action is generally obtained via <a
    *     href="https://docs.oracle.com/en/java/javase/11/docs/api/jdk.management/com/sun/management/GarbageCollectionNotificationInfo.html#getGcAction()">GarbageCollectionNotificationInfo#getGcAction()</a>.
    */
  val JvmGcAction: AttributeKey[String] = string("jvm.gc.action")

  /** Name of the garbage collector.
    *
    * @note
    *   - Garbage collector name is generally obtained via <a
    *     href="https://docs.oracle.com/en/java/javase/11/docs/api/jdk.management/com/sun/management/GarbageCollectionNotificationInfo.html#getGcName()">GarbageCollectionNotificationInfo#getGcName()</a>.
    */
  val JvmGcName: AttributeKey[String] = string("jvm.gc.name")

  /** Name of the memory pool.
    *
    * @note
    *   - Pool names are generally obtained via <a
    *     href="https://docs.oracle.com/en/java/javase/11/docs/api/java.management/java/lang/management/MemoryPoolMXBean.html#getName()">MemoryPoolMXBean#getName()</a>.
    */
  val JvmMemoryPoolName: AttributeKey[String] = string("jvm.memory.pool.name")

  /** The type of memory.
    */
  val JvmMemoryType: AttributeKey[String] = string("jvm.memory.type")

  /** Whether the thread is daemon or not.
    */
  val JvmThreadDaemon: AttributeKey[Boolean] = boolean("jvm.thread.daemon")

  /** State of the thread.
    */
  val JvmThreadState: AttributeKey[String] = string("jvm.thread.state")
  // Enum definitions

  /** Values for [[JvmMemoryType]].
    */
  abstract class JvmMemoryTypeValue(val value: String)
  object JvmMemoryTypeValue {

    /** Heap memory. */
    case object Heap extends JvmMemoryTypeValue("heap")

    /** Non-heap memory. */
    case object NonHeap extends JvmMemoryTypeValue("non_heap")
  }

  /** Values for [[JvmThreadState]].
    */
  abstract class JvmThreadStateValue(val value: String)
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
