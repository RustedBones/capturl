package fr.davit.capturl.parsers
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
    val res = parse("/one/../path?query")

    parse("/?query") shouldBe Path.root                                -> "?query"
    parse("?query") shouldBe Path.empty                                -> "?query"
    parse("/absolute/path?query") shouldBe Path.Resource("/absolute/path") -> "?query"
    parse("relative/path?query") shouldBe Path.Resource("relative/path")   -> "?query"
    parse("directory/?query") shouldBe Path.Directory("directory")     -> "?query"

    // normalization
    parse("/one//path?query") shouldBe Path.Resource("/one/path")       -> "?query"
    parse("/one//path/?query") shouldBe Path.Directory("/one/path") -> "?query"

    parse("/one/./path?query") shouldBe Path.Resource("/one/path")       -> "?query"
    parse("/one/./path/?query") shouldBe Path.Directory("/one/path") -> "?query"

    parse("/one/../path?query") shouldBe Path.Resource("/path")       -> "?query"
    parse("/one/../path/?query") shouldBe Path.Directory("/path") -> "?query"

    parse("/../path?query") shouldBe Path.Resource("/path")       -> "?query"
    parse("/../path/?query") shouldBe Path.Directory("/path") -> "?query"

    parse("./path?query") shouldBe Path.Resource("path")       -> "?query"
    parse("./path/?query") shouldBe Path.Directory("path") -> "?query"

    parse("../path?query") shouldBe Path.Resource("../path")       -> "?query"
    parse("../path/?query") shouldBe Path.Directory("../path") -> "?query"
  }

}
