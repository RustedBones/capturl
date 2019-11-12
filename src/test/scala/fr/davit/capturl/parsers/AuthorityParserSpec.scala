package fr.davit.capturl.parsers

import fr.davit.capturl.scaladsl.Authority.{Port, UserInfo}
import fr.davit.capturl.scaladsl.Host.NamedHost
import fr.davit.capturl.parsers.ParserFixture.TestParser
import fr.davit.capturl.scaladsl.{Authority, Host}
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

    parseError("") shouldBe """Unexpected end of input, expected port (line 1, column 1):
                              |
                              |^""".stripMargin

    parseError("noport") shouldBe """Invalid input 'n', expected port (line 1, column 1):
                                    |noport
                                    |^""".stripMargin

    // TODO improve this error message
    parseError("10000000000") shouldBe """Unexpected end of input, expected port (line 1, column 12):
                                         |10000000000
                                         |           ^""".stripMargin
  }

  it should "parse authority" in new AuthorityFixture {
    parse("") shouldBe Authority.empty -> ""
    parse("/path") shouldBe Authority.empty -> "/path"
    parse("example.com/path") shouldBe Authority(NamedHost("example.com")) -> "/path"
    parse("example.com:80/path") shouldBe Authority(NamedHost("example.com"), Number(80)) -> "/path"
    parse("user:password@example.com/path") shouldBe Authority(NamedHost("example.com"), userInfo = Credentials("user:password")) -> "/path"
    parse("user:password@example.com:80/path") shouldBe Authority(NamedHost("example.com"), Number(80), Credentials("user:password")) -> "/path"

    parse("@example.com/path") shouldBe Authority(NamedHost("example.com"), userInfo = Credentials("")) -> "/path"
  }

}
