package fr.davit.capturl.akka.http

import java.net.IDN

import akka.http.scaladsl.model.Uri
import fr.davit.capturl.scaladsl.Authority.{Port, UserInfo}
import fr.davit.capturl.scaladsl.Path.{Empty, Segment, Slash, SlashOrEmpty}
import fr.davit.capturl.scaladsl._

import scala.annotation.tailrec

trait UriConverters {

  implicit def fromAkkaScheme(scheme: String): Scheme = scheme match {
    case "" => Scheme.Empty
    case _  => Scheme.Protocol(scheme)
  }

  implicit def toAkkaScheme[T <: Scheme](scheme: T): String = scheme.toString

  implicit def fromAkkaHost(host: Uri.Host): Host = host match {
    case Uri.Host.Empty         => Host.Empty
    case Uri.IPv4Host(bytes, _) => Host.IPv4Host(bytes)
    case Uri.IPv6Host(bytes, _) => Host.IPv6Host(bytes)
    case Uri.NamedHost(address) => Host.NamedHost(address)
  }

  implicit def toAkkaHost[T <: Host](host: Host): Uri.Host = host match {
    case Host.Empty              => Uri.Host.Empty
    case Host.IPv4Host(bytes)    => Uri.IPv4Host(bytes.toArray)
    case Host.IPv6Host(bytes)    => Uri.IPv6Host(bytes)
    case Host.NamedHost(address) => Uri.NamedHost(IDN.toASCII(address))
  }

  implicit def fromAkkaUserInfo(userInfo: String): UserInfo = userInfo match {
    case "" => UserInfo.Empty
    case _  => UserInfo.Credentials(userInfo)
  }

  implicit def toAkkaUserInfo[T <: UserInfo](userInfo: T): String = userInfo match {
    case UserInfo.Empty              => ""
    case UserInfo.Credentials(value) => value
  }

  implicit def fromAkkaPort(port: Int): Port = port match {
    case 0 => Port.Empty
    case _ => Port.Number(port)
  }

  implicit def toAkkaPort[T <: Port](port: T): Int = port match {
    case Port.Empty         => 0
    case Port.Number(value) => value
  }

  implicit def fromAkkaAuthority(authority: Uri.Authority): Authority = {
    Authority(authority.host, authority.port, authority.userinfo)
  }

  implicit def toAkkaAuthority(authority: Authority): Uri.Authority = {
    Uri.Authority(authority.host, authority.port, authority.userInfo)
  }

  implicit def fromAkkaPath(path: Uri.Path): Path = {
    @tailrec def pathBuilder(akkaPath: Uri.Path, p: SlashOrEmpty): Path = akkaPath match {
      case Uri.Path.Empty                                  => p
      case Uri.Path.Slash(tail)                            => pathBuilder(tail, Slash(p))
      case Uri.Path.Segment(segment, Uri.Path.Empty)       => Segment(segment, p)
      case Uri.Path.Segment(segment, Uri.Path.Slash(tail)) => pathBuilder(tail, Slash(Segment(segment, p)))
      case _                                               => throw new Exception("Invalid path conversion")
    }

    pathBuilder(path.reverse, Path.Empty)
  }

  implicit def toAkkaPath(path: Path): Uri.Path = {
    @tailrec def akkaPathBuilder(p: Path, akkaPath: Uri.Path.SlashOrEmpty): Uri.Path = p match {
      case Empty                         => akkaPath
      case Slash(tail)                   => akkaPathBuilder(tail, Uri.Path.Slash(akkaPath))
      case Segment(segment, Empty)       => Uri.Path.Segment(segment, akkaPath)
      case Segment(segment, Slash(tail)) => akkaPathBuilder(tail, Uri.Path.Slash(Uri.Path.Segment(segment, akkaPath)))
      case _                             => throw new Exception("Invalid path conversion")
    }
    akkaPathBuilder(path.reverse, Uri.Path.Empty)
  }

  implicit def fromAkkaQuery(query: Uri.Query): Query = {
    val b = Query.newBuilder
    query.foreach { case (k, v) => b.+=((k, if (v.isEmpty) None else Some(v))) }
    b.result()
  }

  implicit def toAkkaQuery[T <: Query](query: T): Uri.Query = {
    val b = Uri.Query.newBuilder
    query.foreach { case (k, v) => b += k -> v.getOrElse("") }
    b.result()
  }

  implicit def toAkkaQueryString[T <: Query](query: T): Option[String] = query match {
    case Query.Empty => None
    case _           => Some(query.toString)
  }

  implicit def fromAkkaFragment(fragment: Option[String]): Fragment = fragment match {
    case None           => Fragment.Empty
    case Some(fragment) => Fragment.Identifier(fragment)
  }

  implicit def toAkkaFragment[T <: Fragment](fragment: T): Option[String] = fragment match {
    case Fragment.Empty             => None
    case Fragment.Identifier(value) => Some(value)
  }

  implicit def fromAkkaUri(uri: Uri): Iri = {
    Iri(uri.scheme, uri.authority, uri.path, uri.query(), uri.fragment)
  }

  implicit def toAkkaUri(iri: Iri): Uri = {
    Uri(iri.scheme, iri.authority, iri.path, iri.query, iri.fragment)
  }

}

object UriConverters extends UriConverters
