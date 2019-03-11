package fr.davit.capturl.parsers

import fr.davit.capturl.scaladsl.Path.{End, Segment, Slash}
import fr.davit.capturl.scaladsl._
import fr.davit.capturl.parsers.ParserFixture.TestParser
import org.parboiled2.ParserInput
import org.scalatest.{FlatSpec, Matchers}

class IriParserSpec  extends FlatSpec with Matchers {

  trait Fixture extends ParserFixture[Iri] {
    override def createParser(input: ParserInput) = new TestParser[Iri](input) with IriParser {
      override def rule = IRI
    }
  }

  "IriParser" should "parse absolute IRI" in new Fixture {
    val iri = Iri(
      Scheme.HTTP,
      Authority(new Host.NamedHost("example.com")),
      Path.Slash(new Path.Segment("path", Path.End)),
      new Query.Part("query", "", Query.Empty),
      new Fragment.Identifier("fragment")
    )
    parse("http://example.com/path?query#fragment") shouldBe iri -> ""
  }

  it should "parse relative IRI" in new Fixture {
    val iri = Iri(
      Scheme.empty,
      Authority.empty,
      Slash(new Segment("path", End)),
      new Query.Part("query", "", Query.Empty),
      new Fragment.Identifier("fragment")
    )
    parse("/path?query#fragment") shouldBe iri -> ""
  }

  it should "not normalize empty query" in new Fixture {
    val iri = Iri(
      Scheme.HTTP,
      Authority(new Host.NamedHost("example.com")),
      Path.Slash(Path.End),
      new Query.Part("", "", Query.Empty),
      Fragment.empty
    )

    parse("http://example.com/?") shouldBe iri -> ""
  }

  it should "not normalize empty fragment" in new Fixture {
    val iri = Iri(
      Scheme.HTTP,
      Authority(new Host.NamedHost("example.com")),
      Path.Slash(Path.End),
      Query.empty,
      Fragment.Identifier("")
    )

    parse("http://example.com/#") shouldBe iri -> ""
  }

}
