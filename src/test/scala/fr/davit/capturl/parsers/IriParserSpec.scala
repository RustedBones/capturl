package fr.davit.capturl.parsers

import fr.davit.capturl.parsers.ParserFixture.TestParser
import fr.davit.capturl.scaladsl.Path.{Segment, Slash}
import fr.davit.capturl.scaladsl._
import org.parboiled2.ParserInput
import org.scalatest.{FlatSpec, Matchers}

class IriParserSpec extends FlatSpec with Matchers {

  trait Fixture extends ParserFixture[Iri] {
    override def createParser(input: ParserInput) = new TestParser[Iri](input) with IriParser {
      override def rule = rule(IRI ~ EOI)
    }
  }

  "IriParser" should "parse absolute IRI" in new Fixture {
    val iri = StrictIri(
      Scheme.HTTP,
      Authority(Host.NamedHost("example.com")),
      Slash(Segment("path")),
      Query.Part("query", None, Query.Empty),
      Fragment.Identifier("fragment")
    )
    parse("http://example.com/path?query#fragment") shouldBe iri -> ""
  }

  it should "parse scheme relative IRI" in new Fixture {
    val iri = StrictIri(
      Scheme.empty,
      Authority(Host.NamedHost("example.com")),
      Slash(Segment("path")),
      Query.Part("query", None, Query.Empty),
      Fragment.Identifier("fragment")
    )
    parse("//example.com/path?query#fragment") shouldBe iri -> ""
  }

  it should "parse host relative IRI" in new Fixture {
    val iri = StrictIri(
      Scheme.empty,
      Authority.empty,
      Slash(Segment("path")),
      Query.Part("query", None, Query.Empty),
      Fragment.Identifier("fragment")
    )
    parse("/path?query#fragment") shouldBe iri -> ""
  }

  it should "parse path relative IRI" in new Fixture {
    val iri = StrictIri(
      Scheme.empty,
      Authority.empty,
      Path.empty,
      Query.Part("query", None, Query.Empty),
      Fragment.Identifier("fragment")
    )
    parse("?query#fragment") shouldBe iri -> ""
  }

  it should "parse query relative IRI" in new Fixture {
    val iri = StrictIri(
      Scheme.empty,
      Authority.empty,
      Path.empty,
      Query.empty,
      Fragment.Identifier("fragment")
    )
    parse("#fragment") shouldBe iri -> ""
  }

  it should "normalize empty path when host/scheme defined" in new Fixture {
    val iri = StrictIri(
      Scheme.HTTP,
      Authority(Host.NamedHost("example.com")),
      Path.root,
      Query.empty,
      Fragment.empty
    )

    parse("http://example.com") shouldBe iri -> ""
  }

  it should "reject invalid IRIs" in new Fixture {
    parseError("http://user{info@example.com/") shouldBe """Invalid input "{i", expected ~, host, port, absolute or empty path, query, fragment or 'EOI' (line 1, column 12):
                                                           |http://user{info@example.com/
                                                           |           ^""".stripMargin

    parseError("http://example.com:-1/") shouldBe """Invalid input ":-1/", expected ~, host, port, absolute or empty path, query, fragment or 'EOI' (line 1, column 19):
                                                    |http://example.com:-1/
                                                    |                  ^""".stripMargin
//    TODO fail on strict
//    parseError("http://example.com/path with space") shouldBe """Invalid input " w", expected absolute or empty path, query, fragment or 'EOI' (line 1, column 24):
//                                                                |http://example.com/path with space
//                                                                |                       ^""".stripMargin
  }

}
