package fr.davit.capturl.parsers

import fr.davit.capturl.Host.NamedHost
import fr.davit.capturl.Path.{End, Segment, Slash}
import fr.davit.capturl._
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
      Authority(new NamedHost("example.com")),
      Slash(new Segment("path", End)),
      new Query("query"),
      new Fragment("fragment")
    )
    parse("http://example.com/path?query#fragment") shouldBe iri -> ""
  }

  it should "parse relative IRI" in new Fixture {

  }

}
