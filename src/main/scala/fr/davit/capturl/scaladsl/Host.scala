package fr.davit.capturl.scaladsl

import java.net.{Inet4Address, Inet6Address}

import fr.davit.capturl.parsers.HostParser
import fr.davit.capturl.scaladsl.OptionalPart.{DefinedPart, EmptyPart}
import org.parboiled2.Parser.DeliveryScheme.Throw

import scala.collection.immutable

sealed abstract class Host extends fr.davit.capturl.javadsl.Host with OptionalPart[String] {

  // default implementation
  override def isIPv4: Boolean = true
  override def isIPv6: Boolean = false
  override def isNamedHost: Boolean = false

  override def asScala(): Host = this
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

    def apply(bytes: Array[Byte]): IPv4Host = apply(bytes.toIndexedSeq)
  }

  final case class IPv4Host(bytes: immutable.Seq[Byte]) extends Host with DefinedPart[String] {
    require(bytes.length == 4, "bytes array must have length 4")
    override lazy val value: String = bytes.map(java.lang.Byte.toUnsignedInt).mkString(".")

    override def getAddress: String = value
    override def isIPv4: Boolean = true
  }

  //--------------------------------------------------------------------------------------------------------------------
  // IPv6Host
  //--------------------------------------------------------------------------------------------------------------------
  object IPv6Host {
    def apply(ip: String): IPv6Host = {
      HostParser(ip).phrase(_.IPv6address)
    }

    def apply(inetAddress: Inet6Address): IPv6Host = apply(inetAddress.getAddress)

    def apply(bytes: Array[Byte]): IPv6Host = apply(bytes.toIndexedSeq)
  }

  final case class IPv6Host(bytes: immutable.Seq[Byte]) extends Host with DefinedPart[String] {
    require(bytes.length == 16, "bytes array must have length 16")
    override lazy val value: String = bytes.map(java.lang.Byte.toUnsignedInt).mkString(":")

    override def getAddress: String = value
    override def isIPv6: Boolean = true
    override def toString: String = s"[$value]"
  }

  //--------------------------------------------------------------------------------------------------------------------
  // NamedHost
  //--------------------------------------------------------------------------------------------------------------------
  final case class NamedHost(value: String) extends Host with DefinedPart[String] {
    override def getAddress: String = value
    override def isNamedHost: Boolean = true
  }

  //--------------------------------------------------------------------------------------------------------------------
  // EmptyHost
  //--------------------------------------------------------------------------------------------------------------------
  case object Empty extends Host with EmptyPart {
    override def getAddress: String = throw new NoSuchElementException("address for empty host")
  }
}
