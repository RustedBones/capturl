package fr.davit.capturl.scaladsl

import java.net.{Inet4Address, Inet6Address}

import fr.davit.capturl.parsers.HostParser
import fr.davit.capturl.scaladsl.OptionalPart.{DefinedPart, EmptyPart}

import scala.collection.immutable
import scala.util.Try

sealed abstract class Host extends fr.davit.capturl.javadsl.Host with OptionalPart[String] {

  def address: String

  // default implementation
  override def isIPv4: Boolean      = true
  override def isIPv6: Boolean      = false
  override def isNamedHost: Boolean = false

  /* Java API */
  override def getAddress: String = address
  override def asScala(): Host    = this
}

object Host {

  val empty: Host = Empty

  def apply(address: String): Host = parse(address).get

  def parse(address: String): Try[Host] = {
    HostParser(address).phrase(_.ihost)
  }

  //--------------------------------------------------------------------------------------------------------------------
  // IPv4Host
  //--------------------------------------------------------------------------------------------------------------------
  object IPv4Host {

    def apply(ip: String): IPv4Host = parse(ip).get

    def parse(ip: String): Try[IPv4Host] = {
      HostParser(ip).phrase(_.IPv4address)
    }

    def apply(byte1: Byte, byte2: Byte, byte3: Byte, byte4: Byte): IPv4Host = {
      apply(Array(byte1, byte2, byte3, byte4))
    }

    def apply(inetAddress: Inet4Address): IPv4Host = apply(inetAddress.getAddress)

    def apply(bytes: Array[Byte]): IPv4Host = apply(bytes.toIndexedSeq)
  }

  final case class IPv4Host(bytes: immutable.Seq[Byte]) extends Host with DefinedPart[String] {
    require(bytes.length == 4, "IPv4 bytes array must have length 4")
    override lazy val value: String = bytes.map(java.lang.Byte.toUnsignedInt).mkString(".")

    override def address: String = value
    override def isIPv4: Boolean = true
  }

  //--------------------------------------------------------------------------------------------------------------------
  // IPv6Host
  //--------------------------------------------------------------------------------------------------------------------
  object IPv6Host {

    def apply(ip: String): IPv6Host = parse(ip).get

    def parse(ip: String): Try[IPv6Host] = {
      HostParser(ip).phrase(_.IPv6address)
    }

    def apply(inetAddress: Inet6Address): IPv6Host = apply(inetAddress.getAddress)

    def apply(bytes: Array[Byte]): IPv6Host = apply(bytes.toIndexedSeq)

    private def hextet(bytes: Seq[Byte]): String = bytes.map(java.lang.Byte.toUnsignedInt) match {
      case Seq(h, l) => ((h << 8) + l).toHexString
      case _ => throw new Exception(s"Can't convert $bytes to hextet")
    }

    private def shorten(hextets: Seq[String]): Seq[String] = {
      var maxIdx = -1
      var maxLength = 0
      var currentIdx = -1
      var currentLength = 0
      hextets.zipWithIndex.foreach {
        case ("0", idx) =>
          if (currentLength == 0) currentIdx = idx
          currentLength += 1
        case _ =>
          if (currentLength > maxLength) {
            maxIdx = currentIdx
            maxLength = currentLength
          }
          currentLength = 0
      }
      if (currentLength > maxLength) {
        maxIdx = currentIdx
        maxLength = currentLength
      }

      if (maxLength > 0) {
        val builder = Seq.newBuilder[String]
        val high = hextets.take(maxIdx)
        val low = hextets.drop(maxIdx + maxLength)
        builder ++= (if (high.isEmpty) Seq("") else high)
        builder += ""
        builder ++= (if (low.isEmpty) Seq("") else low)
        builder.result()
      } else {
        hextets
      }
    }

  }

  final case class IPv6Host(bytes: immutable.Seq[Byte]) extends Host with DefinedPart[String] {
    require(bytes.length == 16, "IPv6 bytes array must have length 16")

    // normaized IPv6 representation: https://tools.ietf.org/html/rfc5952#section-4
    override lazy val value: String = IPv6Host.shorten(bytes.grouped(2).map(IPv6Host.hextet).toSeq).mkString(":")

    override def address: String  = s"[$value]"
    override def isIPv6: Boolean  = true
    override def toString: String = value
  }

  //--------------------------------------------------------------------------------------------------------------------
  // NamedHost
  //--------------------------------------------------------------------------------------------------------------------
  final case class NamedHost(value: String) extends Host with DefinedPart[String] {
    require(value.nonEmpty, "NamedHost can't be empty")

    override def address: String      = value
    override def isNamedHost: Boolean = true
  }

  //--------------------------------------------------------------------------------------------------------------------
  // EmptyHost
  //--------------------------------------------------------------------------------------------------------------------
  case object Empty extends Host with EmptyPart {
    override def address: String = throw new NoSuchElementException("address for empty host")
  }
}
