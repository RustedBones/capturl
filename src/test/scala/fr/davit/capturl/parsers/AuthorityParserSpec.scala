package fr.davit.capturl.parsers

import fr.davit.capturl.scaladsl.Authority.{Port, UserInfo}
import fr.davit.capturl.scaladsl.Host.NamedHost
import fr.davit.capturl.parsers.ParserFixture.TestParser
import fr.davit.capturl.scaladsl.Authority
import fr.davit.capturl.scaladsl.Authority.Port.Number
import fr.davit.capturl.scaladsl.Authority.UserInfo.Credentials
import org.parboiled2.{ParseError, ParserInput}
import org.scalatest.{FlatSpec, Matchers}

class AuthorityParserSpec extends FlatSpec with Matchers {

  trait UserInfoFixture extends ParserFixture[UserInfo] {
    override def createParser(input: ParserInput) = new TestParser[UserInfo](input) with AuthorityParser {
      override def rule = iuserinfo
    }
  }

  trait PortFixture extends ParserFixture[Port] {
    override def createParser(input: ParserInput) = new TestParser[Port](input) with AuthorityParser {
      override def rule = port
    }
  }

  trait AuthorityFixture extends ParserFixture[Authority] {
    override def createParser(input: ParserInput) = new TestParser[Authority](input) with AuthorityParser {
      override def rule = iauthority
    }
  }

  "AuthorityParser" should "parse userinfo" in new UserInfoFixture {
    parse("@host") shouldBe Credentials("") -> "@host"
    parse("userinfo@host") shouldBe Credentials("userinfo") -> "@host"
    parse("%75%73%65%72%69%6E%66%6F@host") shouldBe Credentials("userinfo") -> "@host"
  }

  it should "parse port" in new PortFixture {
    parse("12345/path") shouldBe Number(12345) -> "/path"

    a[ParseError] shouldBe thrownBy(parse("", canThrow = true))
    a[ParseError] shouldBe thrownBy(parse("noport", canThrow = true))
    a[ParseError] shouldBe thrownBy(parse("-1", canThrow = true))
    a[ParseError] shouldBe thrownBy(parse("10000000000", canThrow = true))
  }

  it should "parse authority" in new AuthorityFixture {
    parse("") shouldBe Authority(NamedHost("")) -> ""
    parse("/path") shouldBe Authority(NamedHost("")) -> "/path"
    parse("example.com/path") shouldBe Authority(NamedHost("example.com")) -> "/path"
    parse("example.com:80/path") shouldBe Authority(NamedHost("example.com"), Number(80)) -> "/path"
    parse("user:password@example.com/path") shouldBe Authority(NamedHost("example.com"), userInfo = Credentials("user:password")) -> "/path"
    parse("user:password@example.com:80/path") shouldBe Authority(NamedHost("example.com"), Number(80), Credentials("user:password")) -> "/path"

    parse("@example.com/path") shouldBe Authority(NamedHost("example.com"), userInfo = Credentials("")) -> "/path"
  }

}
