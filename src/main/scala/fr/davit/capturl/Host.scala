package fr.davit.capturl

import java.net.{Inet4Address, Inet6Address}

import fr.davit.capturl.parsers.HostParser
import org.parboiled2.Parser.DeliveryScheme.Throw

import scala.collection.immutable

sealed trait Host

object Host {

  //--------------------------------------------------------------------------------------------------------------------
  // IPv4Host
  //--------------------------------------------------------------------------------------------------------------------
  object IPv4Host {
    def apply(ip: String): IPv4Host = {
      HostParser(ip).IPv4address.run()
    }

    def apply(byte1: Byte, byte2: Byte, byte3: Byte, byte4: Byte): IPv4Host = {
      apply(Array(byte1, byte2, byte3, byte4))
    }

    def apply(inetAddress: Inet4Address): IPv4Host = apply(inetAddress.getAddress)

    def apply(bytes: Array[Byte]): IPv4Host = apply(Vector(bytes.map(java.lang.Byte.toUnsignedInt): _*))
  }

  // internally store bytes as Int because java doe not support unsigned
  final case class IPv4Host(bytes: immutable.Seq[Int]) extends Host {
    require(bytes.length == 4, "bytes array must have length 4")
    require(bytes.forall(b => 0 <= b && b < 256), "invalid byte value")
  }

  //--------------------------------------------------------------------------------------------------------------------
  // IPv6Host
  //--------------------------------------------------------------------------------------------------------------------
  object IPv6Host {
    def apply(ip: String): IPv6Host = {
      HostParser(ip).IPv6address.run()
    }

    def apply(inetAddress: Inet6Address): IPv6Host = apply(inetAddress.getAddress)

    def apply(bytes: Array[Byte]): IPv6Host = apply(Vector(bytes.map(java.lang.Byte.toUnsignedInt): _*))
  }

  // internally store bytes as Int because java doe not support unsigned
  final case class IPv6Host(bytes: immutable.Seq[Int]) extends Host {
    require(bytes.length == 16, "bytes array must have length 16")
    require(bytes.forall(b => 0 <= b && b < 256), "invalid byte value")
  }

  //--------------------------------------------------------------------------------------------------------------------
  // NamedHost
  //--------------------------------------------------------------------------------------------------------------------
  object NamedHost {
    def apply(address: String): NamedHost = {
      HostParser(address).`ireg-name`.run()
    }
  }

  final case class NamedHost private[capturl] (address: String) extends Host
}
