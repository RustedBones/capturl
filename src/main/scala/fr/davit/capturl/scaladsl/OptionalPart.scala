/*
 * Copyright 2019 Michel Davit
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

package fr.davit.capturl.scaladsl

trait OptionalPart[+T] {
  def toOption: Option[T]

  def isEmpty: Boolean
  final def nonEmpty: Boolean = !isEmpty
}

object OptionalPart {

  trait DefinedPart[T] extends OptionalPart[T] {
    protected def value: T

    override def toOption: Option[T] = Some(value)
    override def isEmpty: Boolean    = false
    override def toString: String    = value.toString
  }

  trait EmptyPart extends OptionalPart[Nothing] {
    override def toOption: Option[Nothing] = None
    override def isEmpty: Boolean          = true
    override def toString: String          = ""
  }
}
