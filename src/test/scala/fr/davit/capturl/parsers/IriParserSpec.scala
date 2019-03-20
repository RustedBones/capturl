package fr.davit.capturl.parsers

import fr.davit.capturl.parsers.ParserFixture.TestParser
import fr.davit.capturl.scaladsl.Path.{End, Segment, Slash}
import fr.davit.capturl.scaladsl._
import org.parboiled2.ParserInput
import org.scalatest.{FlatSpec, Matchers}

class IriParserSpec extends FlatSpec with Matchers {

  trait Fixture extends ParserFixture[Iri] {
    override def createParser(input: ParserInput) = new TestParser[Iri](input) with IriParser {
      override def rule = IRI
    }
  }

  "IriParser" should "parse absolute IRI" in new Fixture {

    val iri = Iri(
      Scheme.HTTP,
      Authority(Host.NamedHost("example.com")),
      Path.Slash(Path.Segment("path", Path.End)),
      Query.Part("query", None, Query.Empty),
      Fragment.Identifier("fragment")
    )
    parse("http://example.com/path?query#fragment") shouldBe iri -> ""
  }

  it should "parse scheme relative IRI" in new Fixture {

    val iri = Iri(
      Scheme.empty,
      Authority(Host.NamedHost("example.com")),
      Slash(Segment("path", End)),
      Query.Part("query", None, Query.Empty),
      Fragment.Identifier("fragment")
    )
    parse("//example.com/path?query#fragment") shouldBe iri -> ""
  }

  it should "parse host relative IRI" in new Fixture {

    val iri = Iri(
      Scheme.empty,
      Authority.empty,
      Slash(Segment("path", End)),
      Query.Part("query", None, Query.Empty),
      Fragment.Identifier("fragment")
    )
    parse("/path?query#fragment") shouldBe iri -> ""
  }

  it should "parse path relative IRI" in new Fixture {

    val iri = Iri(
      Scheme.empty,
      Authority.empty,
      Path.empty,
      Query.Part("query", None, Query.Empty),
      Fragment.Identifier("fragment")
    )
    parse("?query#fragment") shouldBe iri -> ""
  }

  it should "parse query relative IRI" in new Fixture {

    val iri = Iri(
      Scheme.empty,
      Authority.empty,
      Path.empty,
      Query.empty,
      Fragment.Identifier("fragment")
    )
    parse("#fragment") shouldBe iri -> ""
  }

  it should "not normalize empty path when host/scheme defined" in new Fixture {

    val iri = Iri(
      Scheme.HTTP,
      Authority(Host.NamedHost("example.com")),
      Path.root,
      Query.empty,
      Fragment.empty
    )

    parse("http://example.com") shouldBe iri -> ""
  }

  it should "not drop empty query" in new Fixture {

    val iri = Iri(
      Scheme.HTTP,
      Authority(Host.NamedHost("example.com")),
      Path.root,
      Query.Part("", None, Query.Empty),
      Fragment.empty
    )

    parse("http://example.com/?") shouldBe iri -> ""
  }

  it should "not drop empty fragment" in new Fixture {

    val iri = Iri(
      Scheme.HTTP,
      Authority(Host.NamedHost("example.com")),
      Path.root,
      Query.empty,
      Fragment.Identifier("")
    )

    parse("http://example.com/#") shouldBe iri -> ""
  }

}
