package fr.davit.capturl.parsers

import fr.davit.capturl.Authority
import fr.davit.capturl.Authority.UserInfo
import fr.davit.capturl.parsers.ParserFixture.TestParser
import org.parboiled2.{ParseError, ParserInput}
import org.scalatest.{FlatSpec, Matchers}

class AuthorityParserSpec extends FlatSpec with Matchers {

  trait UserInfoFixture extends ParserFixture[UserInfo] {
    override def createParser(input: ParserInput) = new TestParser[UserInfo](input) with AuthorityParser {
      override def rule = iuserinfo
    }
  }

  trait PortFixture extends ParserFixture[Int] {
    override def createParser(input: ParserInput) = new TestParser[Int](input) with AuthorityParser {
      override def rule = port
    }
  }

  trait AuthorityFixture extends ParserFixture[Authority] {
    override def createParser(input: ParserInput) = new TestParser[Authority](input) with AuthorityParser {
      override def rule = iauthority
    }
  }

  "AuthorityParser" should "parse userinfo" in new UserInfoFixture {
    parse("userinfo@host") shouldBe UserInfo("userinfo") -> "@host"
    parse("%75%73%65%72%69%6E%66%6F@host") shouldBe UserInfo("userinfo") -> "@host"

    a[ParseError] shouldBe thrownBy(parse("", canThrow = true))
    a[ParseError] shouldBe thrownBy(parse("nodelimiter", canThrow = true))
    a[ParseError] shouldBe thrownBy(parse("invalid char@", canThrow = true))
  }

  it should "parse port" in new PortFixture {
    parse("12345/path") shouldBe 12345 -> "/path"

    a[ParseError] shouldBe thrownBy(parse("", canThrow = true))
    a[ParseError] shouldBe thrownBy(parse("noport", canThrow = true))
  }

  it should "parse authority" in new AuthorityFixture {
    parse("") shouldBe Authority("", None, None) -> ""
    parse("/path") shouldBe Authority("", None, None) -> "/path"
    parse("example.com/path") shouldBe Authority("example.com", None, None) -> "/path"
    parse("example.com:80/path") shouldBe Authority("example.com", Some(80), None) -> "/path"
    parse("user:password@example.com/path") shouldBe Authority("example.com", None, Some(UserInfo("user:password"))) -> "/path"
    parse("user:password@example.com:80/path") shouldBe Authority("example.com", Some(80), Some(UserInfo("user:password"))) -> "/path"
  }

}
