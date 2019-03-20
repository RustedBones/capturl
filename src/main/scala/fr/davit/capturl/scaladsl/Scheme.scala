package fr.davit.capturl.scaladsl

import fr.davit.capturl.parsers.SchemeParser
import fr.davit.capturl.scaladsl.OptionalPart.{DefinedPart, EmptyPart}
import org.parboiled2.Parser.DeliveryScheme.Throw

sealed trait Scheme extends OptionalPart[String]

object Scheme {

  val empty: Scheme = Empty

  case object Empty extends Scheme with EmptyPart
  case class Protocol(value: String) extends Scheme with DefinedPart[String]

  private var registry: Map[Scheme, Authority.Port.Number] = Map.empty

  def register(protocol: Scheme, defaultPort: Int): Unit = {
    require(
      !registry.contains(protocol),
      s"Scheme $protocol is already registered with default port ${registry(protocol)}")
    registry += protocol -> Authority.Port.Number(defaultPort)
  }

  def apply(scheme: String): Scheme = {
    SchemeParser(scheme).phrase(_.scheme)
  }

  def apply(scheme: String, defaultPort: Int): Scheme = {
    val s = apply(scheme)
    register(s, defaultPort)
    s
  }

  def defaultPort(scheme: Scheme): Option[Authority.Port.Number] = registry.get(scheme)

  val Data   = Scheme("data")
  val File   = Scheme("file")
  val FTP    = Scheme("ftp", 21)
  val SSH    = Scheme("ssh", 22)
  val Telnet = Scheme("telnet", 23)
  val SMTP   = Scheme("smtp", 25)
  val Domain = Scheme("domain", 53)
  val TFTP   = Scheme("tftp", 69)
  val HTTP   = Scheme("http", 80)
  val WS     = Scheme("ws", 80)
  val POP3   = Scheme("pop3", 110)
  val NNTP   = Scheme("nntp", 119)
  val IMAP   = Scheme("imap", 143)
  val SNMP   = Scheme("snmp", 161)
  val LDAP   = Scheme("ldap", 389)
  val HTTPS  = Scheme("https", 443)
  val WSS    = Scheme("wss", 443)
  val IMAPS  = Scheme("imaps", 993)
  val NFS    = Scheme("nfs", 2049)

}
