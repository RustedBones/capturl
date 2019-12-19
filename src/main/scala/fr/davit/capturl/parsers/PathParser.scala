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
import fr.davit.capturl.scaladsl.Path
import fr.davit.capturl.scaladsl.Path.{Empty, Segment, Slash, SlashOrEmpty}
import org.parboiled2._

object PathParser {

  def apply(path: String): StringParser with PathParser = {
    new StringParser(path) with PathParser
  }
}

trait PathParser extends RichStringBuilding {
  this: StringParser =>

  def isegment: Rule1[Segment] = rule {
    atomic {
      clearSB() ~ ipchar.* ~ push(Segment(sb.toString))
    }
  }

  def `isegment-nz`: Rule1[Segment] = rule {
    atomic {
      clearSB() ~ ipchar.+ ~ push(Segment(sb.toString))
    }
  }

  def `isegment-nz-nc ` : Rule1[Segment] = rule {
    atomic {
      clearSB() ~ (iunreserved | `pct-encoded` | `sub-delims` | '@' ~ appendSB()).+ ~ push(Segment(sb.toString))
    }
  }

  def `ipath-abempty`: Rule1[SlashOrEmpty] = rule {
    // construct in reverse for better performance
    push(Empty) ~ ('/' ~ isegment ~> ((p: Path, s: Segment) => s.copy(tail = Slash(p)))).* ~> { (p: Path) =>
      p.reverse match {
        case abempty: SlashOrEmpty => abempty
        case path                  => throw new Exception(s"Path '$path' is not abempty")
      }
    }
  }

  def `ipath-absolute`: Rule1[Slash] = rule {
    '/' ~ push(Slash()) ~ (`isegment-nz` ~ `ipath-abempty` ~> (
        (
            r: Slash,
            s: Segment,
            p: SlashOrEmpty
        ) => r.copy(tail = s.copy(tail = p))
    )).?
  }

  def `ipath-noscheme`: Rule1[Segment] = rule {
    `isegment-nz-nc ` ~ `ipath-abempty` ~> ((s: Segment, p: SlashOrEmpty) => s.copy(tail = p))
  }

  def `ipath-rootless`: Rule1[Segment] = rule {
    `isegment-nz` ~ `ipath-abempty` ~> ((s: Segment, p: SlashOrEmpty) => s.copy(tail = p))
  }

  def `ipath-empty`: Rule1[Empty.type] = rule {
    clearSB() ~ &(!ipchar) ~ push(Empty)
  }

  def ipath: Rule1[Path] = rule {
    (`ipath-rootless` | `ipath-absolute` | `ipath-abempty` | `ipath-empty`) ~> ((path: Path) => path.normalize())
  }
}
