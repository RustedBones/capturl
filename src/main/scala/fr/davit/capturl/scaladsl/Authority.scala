package fr.davit.capturl.scaladsl

import java.util.Optional

import fr.davit.capturl.javadsl
import fr.davit.capturl.parsers.AuthorityParser
import fr.davit.capturl.scaladsl.Authority.{Port, UserInfo}
import fr.davit.capturl.scaladsl.OptionalPart.{DefinedPart, EmptyPart}
import org.parboiled2.Parser.DeliveryScheme.Throw

import scala.compat.java8.OptionConverters._

final case class Authority(host: Host, port: Port = Port.empty, userInfo: UserInfo = UserInfo.empty)
    extends javadsl.Authority {
  def isEmpty: Boolean  = host.isEmpty
  def nonEmpty: Boolean = !isEmpty

  override def getHost: javadsl.Host         = host
  override def getPort: Optional[Integer]    = port.toOption.map(p => p: Integer).asJava
  override def getUserInfo: Optional[String] = userInfo.toOption.asJava

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

  def apply(authority: String): Authority = {
    AuthorityParser(authority).phrase(_.iauthority)
  }

  trait Port extends OptionalPart[Int]

  object Port {
    val MaxPortNumber = 65535

    val empty: Port = Empty

    case object Empty extends Port with EmptyPart
    final case class Number(value: Int) extends Port with DefinedPart[Int] {
      require(0 <= value && value < MaxPortNumber, s"Invalid port number '$value'")
    }
  }

  trait UserInfo extends OptionalPart[String]

  object UserInfo {
    val empty: UserInfo = Empty

    def apply(userInfo: String): UserInfo = {
      AuthorityParser(userInfo).phrase(_.iuserinfo)
    }

    case object Empty extends UserInfo with EmptyPart
    final case class Credentials(value: String) extends UserInfo with DefinedPart[String]
  }
}
