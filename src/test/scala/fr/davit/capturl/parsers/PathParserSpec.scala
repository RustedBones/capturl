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
import fr.davit.capturl.parsers.ParserFixture.TestParser
import fr.davit.capturl.scaladsl.Path
import fr.davit.capturl.scaladsl.Path.{Segment, Slash}
import org.parboiled2.ParserInput
import org.scalatest.{FlatSpec, Matchers}

class PathParserSpec extends FlatSpec with Matchers {

  trait Fixture extends ParserFixture[Path] {
    override def createParser(input: ParserInput) = new TestParser[Path](input) with PathParser {
      override def rule = ipath
    }
  }

  "PathParser" should "parse path" in new Fixture {
    parse("?query") shouldBe Path.empty                                                       -> "?query"
    parse("/?query") shouldBe Path.root                                                       -> "?query"
    parse("?query") shouldBe Path.empty                                                       -> "?query"
    parse("/absolute/path?query") shouldBe Slash(Segment("absolute", Slash(Segment("path")))) -> "?query"
    parse("relative/path?query") shouldBe Segment("relative", Slash(Segment("path")))         -> "?query"
    parse("directory/?query") shouldBe Path.Segment("directory", Slash())                     -> "?query"
    parse("/one//path?query") shouldBe Slash(Segment("one", Slash(Slash(Segment("path")))))   -> "?query"
    parse("/%C3%B6?query") shouldBe Slash(Segment("ö"))                                       -> "?query"

    // relax parsing
    parse("/path with spaces?query") shouldBe Slash(Segment("path with spaces")) -> "?query"
  }

}
