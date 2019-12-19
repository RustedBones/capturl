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

import fr.davit.capturl.parsers.FragmentParser
import fr.davit.capturl.scaladsl.OptionalPart.{DefinedPart, EmptyPart}

import scala.util.{Success, Try}

sealed trait Fragment extends OptionalPart[String]

object Fragment {

  val empty: Fragment = Empty

  def apply(fragment: String): Fragment = parse(fragment).get

  def parse(fragment: String): Try[Fragment] = {
    if (fragment.isEmpty) {
      Success(Fragment.Empty)
    } else {
      FragmentParser(fragment).phrase(_.ifragment)
    }
  }

  case object Empty extends Fragment with EmptyPart
  final case class Identifier(value: String) extends Fragment with DefinedPart[String]
}
