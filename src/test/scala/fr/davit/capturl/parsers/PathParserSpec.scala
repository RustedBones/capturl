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
    parse("/?query") shouldBe Path.root                                                       -> "?query"
    parse("?query") shouldBe Path.empty                                                       -> "?query"
    parse("/absolute/path?query") shouldBe Slash(Segment("absolute", Slash(Segment("path")))) -> "?query"
    parse("relative/path?query") shouldBe Segment("relative", Slash(Segment("path")))         -> "?query"

    // unnormalized paths
    parse("directory/?query") shouldBe Path.Segment("directory", Slash(Segment("")))                     -> "?query"
    parse("/one//path?query") shouldBe Slash(Segment("one", Slash(Segment("", Slash(Segment("path")))))) -> "?query"
  }

}
