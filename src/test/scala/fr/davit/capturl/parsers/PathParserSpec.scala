package fr.davit.capturl.parsers
import fr.davit.capturl.scaladsl.Path.{End, Segment, Slash}
import fr.davit.capturl.parsers.ParserFixture.TestParser
import fr.davit.capturl.scaladsl.Path
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
    parse("?query") shouldBe End                                                                           -> "?query"
    parse("/absolute/path?query") shouldBe Slash(Segment("absolute", Slash(Segment("path", End)))) -> "?query"
    parse("relative/path?query") shouldBe Segment("relative", Slash(Segment("path", End)))         -> "?query"

    // normalization
    parse("/one//path?query") shouldBe Slash(Segment("one", Slash(Segment("path", End))))  -> "?query"
    parse("/one/./path?query") shouldBe Slash(Segment("one", Slash(Segment("path", End)))) -> "?query"
    parse("/one/../path?query") shouldBe Slash(Segment("path", End))                           -> "?query"
    parse("/../path?query") shouldBe Slash(Segment("path", End))                               -> "?query"
    parse("./path?query") shouldBe Segment("path", End)                                        -> "?query"
    parse("../path?query") shouldBe Segment("..", Slash(Segment("path", End)))             -> "?query"
  }

}
