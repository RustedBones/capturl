package fr.davit.capturl

import fr.davit.capturl.Authority.UserInfo

final case class Authority(host: String, port: Option[Int],  userInfo: Option[UserInfo])

object Authority {
  final case class UserInfo(value: String)
}
