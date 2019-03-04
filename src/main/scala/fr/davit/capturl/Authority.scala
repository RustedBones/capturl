package fr.davit.capturl

import fr.davit.capturl.Authority.UserInfo
import fr.davit.capturl.parsers.AuthorityParser
import org.parboiled2.Parser.DeliveryScheme.Throw

final case class Authority(host: Host, port: Int = -1, userInfo: UserInfo = UserInfo.empty)

object Authority {

  val empty: Authority = Authority(Host.Empty)

  final case class UserInfo private[capturl] (value: String)

  object UserInfo {

    val empty: UserInfo = new UserInfo("")

    def apply(userInfo: String): UserInfo = {
      AuthorityParser(userInfo).phrase(_.iuserinfo)
    }
  }
}
