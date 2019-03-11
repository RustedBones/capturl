package fr.davit.capturl.scaladsl

import fr.davit.capturl.parsers.AuthorityParser
import fr.davit.capturl.scaladsl.Authority.{Port, UserInfo}
import org.parboiled2.Parser.DeliveryScheme.Throw

final case class Authority(host: Host, port: Port = Port.empty, userInfo: UserInfo = UserInfo.empty) {
  def isEmpty: Boolean  = host.isEmpty
  def nonEmpty: Boolean = !isEmpty

  def normalize(scheme: Scheme): Authority = port match {
    case Port.Number(value) if Scheme.defaultPort(scheme).contains(value) => copy(port = Port.empty)
    case _                                                                => this
  }

  override def toString: String = {
    val b = StringBuilder.newBuilder
    if (userInfo.nonEmpty) b.append(s"$userInfo@")
    b.append(host)
    if (port.nonEmpty) b.append(s":$port")
    b.toString
  }
}

object Authority {

  val empty: Authority = Authority(Host.Empty)

  trait Port {
    def isEmpty: Boolean
    def nonEmpty: Boolean = !isEmpty
  }

  object Port {
    val MaxPortNumber = 65535

    val empty: Port = Empty

    case object Empty extends Port {
      override def isEmpty: Boolean = true
      override def toString: String = ""
    }

    final case class Number(value: Int) extends Port {
      require(0 <= value && value < MaxPortNumber, s"Invalid port number '$value'")
      override def isEmpty: Boolean = false
      override def toString: String = value.toString

    }
  }

  trait UserInfo {
    def isEmpty: Boolean
    def nonEmpty: Boolean = !isEmpty
  }

  object UserInfo {

    val empty: UserInfo = Empty

    def apply(userInfo: String): UserInfo = {
      AuthorityParser(userInfo).phrase(_.iuserinfo)
    }

    case object Empty extends UserInfo {
      override def isEmpty: Boolean = true
      override def toString: String = ""
    }

    final case class Credentials private[capturl] (value: String) extends UserInfo {
      override def isEmpty: Boolean = false
      override def toString: String = value
    }
  }
}
