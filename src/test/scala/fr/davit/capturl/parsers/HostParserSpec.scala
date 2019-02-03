package fr.davit.capturl.parsers

import fr.davit.capturl.parsers.ParserFixture.TestParser
import org.parboiled2.{ParseError, ParserInput}
import org.scalatest.{FlatSpec, Matchers}

class HostParserSpec extends FlatSpec with Matchers {

  trait Fixture extends ParserFixture[String] {
    override def createParser(input: ParserInput) = new TestParser[String](input) with HostParser {
      override def rule = ihost
    }
  }

  "HostParser" should "parse IPv4 hosts" in new Fixture {
    parse("0.0.0.0:80") shouldBe "0.0.0.0" -> ":80"
    parse("255.255.255.255/path") shouldBe "255.255.255.255" -> "/path"
    parse("09.09.09.09") shouldBe "9.9.9.9" -> ""

    a[ParseError] shouldBe thrownBy(parse("256.256.256.256", canThrow = true))
  }

  it should "parse domains" in new Fixture {
    parse("") shouldBe "" -> ""
    parse("example.com:80") shouldBe "example.com" -> ":80"
    parse("Example.COM/path") shouldBe "example.com" -> "/path"
    parse("bücher.example") shouldBe "bücher.example" -> ""
    parse("ἀῼ") shouldBe "ἀῳ" -> "" // lower case unicode extended
  }

}
