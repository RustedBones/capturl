package fr.davit.capturl.parsers

import fr.davit.capturl.Scheme
import fr.davit.capturl.parsers.ParserFixture.TestParser
import org.parboiled2.{ParseError, ParserInput}
import org.scalatest.{FlatSpec, Matchers}

class SchemeParserSpec extends FlatSpec with Matchers {

  trait Fixture extends ParserFixture[Scheme] {
    override def createParser(input: ParserInput) = new TestParser[Scheme](input) with SchemeParser {
      override def rule = scheme
    }
  }

  "SchemeParser" should "parse scheme" in new Fixture {
    parse("file://my_file.txt") shouldBe Scheme.File -> "://my_file.txt"
    parse("http://example.com") shouldBe Scheme.HTTP -> "://example.com"
    parse("data:123456") shouldBe Scheme("data") -> ":123456"

    parse("HTTP://example.com") shouldBe Scheme.HTTP -> "://example.com"

    a[ParseError] shouldBe thrownBy(parse("", canThrow = true))
    a[ParseError] shouldBe thrownBy(parse("$invalid", canThrow = true))
  }
}
