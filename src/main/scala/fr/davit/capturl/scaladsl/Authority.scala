package fr.davit.capturl.scaladsl

import fr.davit.capturl.parsers.AuthorityParser
import fr.davit.capturl.scaladsl.Authority.UserInfo
import org.parboiled2.Parser.DeliveryScheme.Throw

final case class Authority(host: Host, port: Int = -1, userInfo: UserInfo = UserInfo.empty) {
  def isEmpty: Boolean  = host.isEmpty
  def nonEmpty: Boolean = !isEmpty

  def normalize(scheme: Scheme): Authority = {
    if (port > 0 && Scheme.defaultPort(scheme).contains(port)) copy(port = -1)
    else this
  }

  // userinfo will not be not rendered on toString
  override def toString: String = if (port > 0) s"$host:$port" else host.toString
}

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
