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
import fr.davit.capturl.scaladsl.Query
import org.parboiled2.{CharPredicate, Rule0, Rule1}

object QueryParser {

  def apply(path: String): StringParser with QueryParser = {
    new StringParser(path) with QueryParser
  }
}

trait QueryParser extends RichStringBuilding { this: StringParser =>

  protected lazy val `part-sub-delims-predicate`: CharPredicate = {
    `sub-delims-predicate` -- CharPredicate('&', '+') ++ CharPredicate(':', '@', '/', '?')
  }

  def spaces: Rule0 = rule {
    // TODO allow space only in 'relax' mode
    CharPredicate('+', ' ') ~ appendSB(' ')
  }

  def `part-sub-delims`: Rule0 = rule {
    `part-sub-delims-predicate` ~ appendSB()
  }

  def `part-char`: Rule0 = rule {
    iunreserved | `pct-encoded` | spaces | `part-sub-delims`
  }

  def `query-part`: Rule1[String] = rule {
    atomic {
      clearSB() ~ `part-char`.* ~ push(sb.toString)
    }
  }

  def iquery: Rule1[Query] = rule {
    `query-part`.*.separatedBy('&') ~> { parts: Seq[String] =>
      val b = Query.newBuilder
      parts.foreach { p =>
        val keyValue = p.split("=")
        val key      = keyValue.head
        val value    = keyValue.drop(1).headOption
        b += key -> value
      }
      b.result()
    }
  }
}
