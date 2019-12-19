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

package fr.davit.capturl.parsers
import fr.davit.capturl.scaladsl.Scheme
import fr.davit.capturl.scaladsl.Scheme.Protocol
import org.parboiled2.CharPredicate._
import org.parboiled2.Rule1

object SchemeParser {
  private val SchemeChars = AlphaNum ++ '+' ++ '-' ++ '.'

  def apply(scheme: String): StringParser with SchemeParser = {
    new StringParser(scheme) with SchemeParser
  }
}

trait SchemeParser extends RichStringBuilding { this: StringParser =>

  import SchemeParser._

  def scheme: Rule1[Scheme] = rule {
    atomic {
      clearSB() ~ Alpha ~ appendLowered() ~ (SchemeChars ~ appendLowered()).* ~
        push(Protocol(sb.toString))
    }
  }
}
