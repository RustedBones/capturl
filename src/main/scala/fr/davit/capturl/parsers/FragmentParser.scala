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

import fr.davit.capturl.scaladsl.Fragment
import fr.davit.capturl.scaladsl.Fragment.Identifier
import org.parboiled2.{CharPredicate, Rule1}

object FragmentParser {

  def apply(fragment: String): StringParser with FragmentParser = {
    new StringParser(fragment) with FragmentParser
  }
}

trait FragmentParser extends RichStringBuilding { this: StringParser =>

  def ifragment: Rule1[Fragment] = rule {
    atomic {
      clearSB() ~ (ipchar | CharPredicate('/', '?') ~ appendSB()).* ~ push(Identifier(sb.toString))
    }
  }

}
