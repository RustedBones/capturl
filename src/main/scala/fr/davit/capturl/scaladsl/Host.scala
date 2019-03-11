package fr.davit.capturl.scaladsl

import java.net.{Inet4Address, Inet6Address}

import fr.davit.capturl.parsers.HostParser
import org.parboiled2.Parser.DeliveryScheme.Throw

import scala.collection.immutable

sealed abstract class Host extends fr.davit.capturl.javadsl.Host {

  // default implementation
  override def isIPv4: Boolean = true
  override def isIPv6: Boolean = false
  override def isNamedHost: Boolean = false
  override def isEmpty: Boolean = false
  def nonEmpty: Boolean = !isEmpty
}

object Host {

  val empty: Host = Empty

  def apply(address: String): Host = {
    HostParser(address).phrase(_.ihost)
  }

  //--------------------------------------------------------------------------------------------------------------------
  // IPv4Host
  //--------------------------------------------------------------------------------------------------------------------
  object IPv4Host {
    def apply(ip: String): IPv4Host = {
      HostParser(ip).phrase(_.IPv4address)
    }

    def apply(byte1: Byte, byte2: Byte, byte3: Byte, byte4: Byte): IPv4Host = {
      apply(Array(byte1, byte2, byte3, byte4))
    }

    def apply(inetAddress: Inet4Address): IPv4Host = apply(inetAddress.getAddress)

    def apply(bytes: Array[Byte]): IPv4Host = apply(Vector(bytes: _*))
  }

  final case class IPv4Host(bytes: immutable.Seq[Byte]) extends Host {
    require(bytes.length == 4, "bytes array must have length 4")

    override def address: String = bytes.map(java.lang.Byte.toUnsignedInt).mkString(".")
    override def isIPv4: Boolean = true
    override def toString: String = address
  }

  //--------------------------------------------------------------------------------------------------------------------
  // IPv6Host
  //--------------------------------------------------------------------------------------------------------------------
  object IPv6Host {
    def apply(ip: String): IPv6Host = {
      HostParser(ip).phrase(_.IPv6address)
    }

    def apply(inetAddress: Inet6Address): IPv6Host = apply(inetAddress.getAddress)

    def apply(bytes: Array[Byte]): IPv6Host = apply(Vector(bytes: _*))
  }

  final case class IPv6Host(bytes: immutable.Seq[Byte]) extends Host {
    require(bytes.length == 16, "bytes array must have length 16")

    override def address: String = bytes.map(java.lang.Byte.toUnsignedInt).mkString(":")
    override def isIPv6: Boolean = true
    override def toString: String = s"[$address]"
  }

  //--------------------------------------------------------------------------------------------------------------------
  // NamedHost
  //--------------------------------------------------------------------------------------------------------------------
  final case class NamedHost private[capturl] (override val address: String) extends Host {
    override def isNamedHost: Boolean = true
    override def toString: String = address
  }

  object NamedHost {
    def apply(address: String): NamedHost = {
      HostParser(address).phrase(_.`ireg-name`)
    }
  }

  //--------------------------------------------------------------------------------------------------------------------
  // EmptyHost
  //--------------------------------------------------------------------------------------------------------------------
  case object Empty extends Host {
    override def address(): String = throw new NoSuchElementException("address for empty host")
    override def isEmpty: Boolean = true
    override def toString: String = ""
  }
}
