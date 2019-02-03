package fr.davit.capturl.parsers

import fr.davit.capturl.Authority
import fr.davit.capturl.Authority.UserInfo
import org.parboiled2.CharPredicate._
import org.parboiled2.{Parser, Rule0, Rule1, StringBuilding}


trait AuthorityParser extends HostParser { this: Parser with StringBuilding =>

  def port: Rule1[Int] = rule {
    capture(Digit.+) ~> ((s: String) => s.toInt)
  }

  def iuserinfo: Rule1[UserInfo] = rule {
    clearSB() ~
      (iunreserved | `pct-encoded` | `sub-delims` | ':' ~ appendSB()).* ~ &('@') ~
      push(UserInfo(sb.toString))
  }

  def iauthority: Rule1[Authority] = rule {
    ((iuserinfo ~ '@').? ~ ihost ~ (':' ~ port).?) ~> { (u: Option[UserInfo], h: String, p: Option[Int]) =>
      Authority(h, p, u)
    }
  }
}
