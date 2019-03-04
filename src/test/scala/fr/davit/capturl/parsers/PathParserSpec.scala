package fr.davit.capturl.parsers

import fr.davit.capturl.Path
import fr.davit.capturl.Path.{End, Segment, Slash}
import fr.davit.capturl.parsers.ParserFixture.TestParser
import org.parboiled2.ParserInput
import org.scalatest.{FlatSpec, Matchers}

class PathParserSpec extends FlatSpec with Matchers {

  trait Fixture extends ParserFixture[Path] {
    override def createParser(input: ParserInput) = new TestParser[Path](input) with PathParser {
      override def rule = ipath
    }
  }

  "PathParser" should "parse path" in new Fixture {
    parse("/?query") shouldBe Slash(End)                                                                   -> "?query"
    parse("/absolute/path?query") shouldBe Slash(new Segment("absolute", Slash(new Segment("path", End)))) -> "?query"
    parse("relative/path?query") shouldBe new Segment("relative", Slash(new Segment("path", End)))         -> "?query"

    // normalization
    parse("?query") shouldBe Slash(End)                                                            -> "?query"
    parse("/one/./path?query") shouldBe Slash(new Segment("one", Slash(new Segment("path", End)))) -> "?query"
    parse("/one/../path?query") shouldBe Slash(new Segment("path", End))                           -> "?query"
    parse("/../path?query") shouldBe Slash(new Segment("path", End))                               -> "?query"
    parse("./path?query") shouldBe new Segment("path", End)                                        -> "?query"
    parse("../path?query") shouldBe new Segment("..", Slash(new Segment("path", End)))             -> "?query"
  }

}
