package fr.davit.capturl.parsers

import fr.davit.capturl.parsers.ParserFixture.TestParser
import fr.davit.capturl.scaladsl.Query
import org.parboiled2.ParserInput
import org.scalatest.{FlatSpec, Matchers}

class QueryParserSpec extends FlatSpec with Matchers {

  trait Fixture extends ParserFixture[Query] {
    override def createParser(input: ParserInput) = new TestParser[Query](input) with QueryParser {
      override def rule = iquery
    }
  }

  "QueryParser" should "parse query" in new Fixture {
    parse("#fragment") shouldBe Query.Part("") -> "#fragment"
    parse("key1=val1&key2#fragment") shouldBe Query.Part("key1", Some("val1"), Query.Part("key2")) -> "#fragment"

    // relax parsing
    parse("key with+spaces#fragment") shouldBe Query.Part("key with spaces") -> "#fragment"
  }

}
