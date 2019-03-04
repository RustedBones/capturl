package fr.davit.capturl.parsers

import fr.davit.capturl.Authority.UserInfo
import fr.davit.capturl.{Authority, Host}
import org.parboiled2.CharPredicate._
import org.parboiled2.{Parser, Rule1}

import scala.util.{Success, Try}

object AuthorityParser {

  val MaxPort = 65535

  def apply(userInfo: String): Parser with AuthorityParser = {
    new StringParser(userInfo) with AuthorityParser
  }
}

trait AuthorityParser extends HostParser { this: Parser =>

  def port: Rule1[Int] = rule {
    capture(Digit.+) ~> { s: String =>
      Try(s.toInt) match {
        case Success(value) if value < AuthorityParser.MaxPort => push(value)
        case _                                                 => failX(s"Invalid port '$s'")
      }
    }
  }

  def iuserinfo: Rule1[UserInfo] = rule {
    clearSB() ~
      (iunreserved | `pct-encoded` | `sub-delims` | ':' ~ appendSB()).* ~
      push(new UserInfo(sb.toString))
  }

  def iauthority: Rule1[Authority] = rule {
    ((iuserinfo ~ '@').? ~ ihost ~ (':' ~ port).?) ~> { (u: Option[UserInfo], h: Host, p: Option[Int]) =>
      Authority(h, p.getOrElse(-1), u.getOrElse(UserInfo.empty))
    }
  }
}
