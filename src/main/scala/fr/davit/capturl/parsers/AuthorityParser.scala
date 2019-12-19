/*
 * Copyright 2019 Michel Davit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.davit.capturl.parsers

import fr.davit.capturl.scaladsl.Authority.{Port, UserInfo}
import fr.davit.capturl.scaladsl.{Authority, Host}
import org.parboiled2.CharPredicate._
import org.parboiled2.Rule1

import scala.util.{Success, Try}

object AuthorityParser {

  def apply(userInfo: String): StringParser with AuthorityParser = {
    new StringParser(userInfo) with AuthorityParser
  }
}

trait AuthorityParser extends HostParser { this: StringParser =>

  def port: Rule1[Port] = rule {
    atomic {
      capture(Digit.+) ~> { s: String =>
        Try(s.toInt) match {
          case Success(value) if value < Port.MaxPortNumber => push(Port.Number(value))
          case _                                            => failX("port")
        }
      }
    }
  }

  def iuserinfo: Rule1[UserInfo] = rule {
    atomic {
      clearSB() ~ (iunreserved | `pct-encoded` | `sub-delims` | ':' ~ appendSB()).* ~
        push(UserInfo.Credentials(sb.toString))
    }
  }

  def iauthority: Rule1[Authority] = rule {
    ((iuserinfo ~ '@').? ~ ihost ~ (':' ~ port).? ~> { (u: Option[UserInfo], h: Host, p: Option[Port]) =>
      Authority(h, p.getOrElse(Port.empty), u.getOrElse(UserInfo.empty))
    })
  }
}
