package fr.davit.capturl.scaladsl

import java.util.{Objects, Optional}

import fr.davit.capturl.javadsl
import fr.davit.capturl.parsers.IriParser
import fr.davit.capturl.scaladsl.Iri.{normalizeAuthority, normalizePath}
import org.parboiled2.Parser.DeliveryScheme.Throw

import scala.compat.java8.OptionConverters._

final class Iri private[capturl] (
    val scheme: Scheme,
    val authority: Authority,
    val path: Path,
    val query: Query,
    val fragment: Fragment
) extends javadsl.Iri {
  def isEmpty: Boolean    = scheme.isEmpty && authority.isEmpty && path.isEmpty && query.isEmpty && fragment.isEmpty
  def isAbsolute: Boolean = scheme.nonEmpty
  def isRelative: Boolean = !isAbsolute

  override def toString: String = {
    val b = new StringBuilder()
    if (scheme.nonEmpty) b.append(s"$scheme://")
    if (authority.nonEmpty) b.append(authority)
    if (path.nonEmpty) b.append(path)
    if (query.nonEmpty) b.append(s"?$query")
    if (fragment.nonEmpty) b.append(s"#$fragment")
    b.toString
  }

  /* Java API */
  override def getScheme: String               = scheme.toString
  override def getAuthority: javadsl.Authority = authority
  override def getPath: javadsl.Path           = path
  override def getQuery: javadsl.Query         = query
  override def getFragment: Optional[String]   = fragment.toOption.asJava

  override def equals(o: Any): Boolean = o match {
    case that: Iri =>
      Objects.equals(this.scheme, that.scheme) &&
        Objects.equals(this.authority, that.authority) &&
        Objects.equals(this.path, that.path) &&
        Objects.equals(this.query, that.query) &&
        Objects.equals(this.fragment, that.fragment)
    case _ => false
  }

  override def hashCode(): Int = Objects.hash(scheme, authority, path, query, fragment)

  def copy(
      scheme: Scheme = this.scheme,
      authority: Authority = this.authority,
      path: Path = this.path,
      query: Query = this.query,
      fragment: Fragment = this.fragment
  ): Iri = {
    val normalizedAuthority = normalizeAuthority(scheme, authority)
    val normalizedPath      = normalizePath(scheme, authority, path)
    new Iri(scheme, normalizedAuthority, normalizedPath, query, fragment)
  }
}

object Iri {

  private def normalizeAuthority(scheme: Scheme, authority: Authority): Authority = {
    Scheme.defaultPort(scheme) match {
      case Some(port) => authority.copy(port = port)
      case None       => authority
    }
  }

  private def normalizePath(scheme: Scheme, authority: Authority, path: Path): Path = {
    if ((scheme.nonEmpty || authority.nonEmpty) && path.isEmpty) Path.root
    else path
  }

  def apply(
      scheme: Scheme = Scheme.empty,
      authority: Authority = Authority.empty,
      path: Path = Path.empty,
      query: Query = Query.empty,
      fragment: Fragment = Fragment.empty): Iri = {
    val normalizedAuthority = normalizeAuthority(scheme, authority)
    val normalizedPath      = normalizePath(scheme, authority, path)
    new Iri(scheme, normalizedAuthority, normalizedPath, query, fragment)
  }

  def apply(iri: String): Iri = {
    val rawIri              = IriParser(iri).phrase(_.IRI)
    val normalizedAuthority = normalizeAuthority(rawIri.scheme, rawIri.authority)
    val normalizedPath      = normalizePath(rawIri.scheme, rawIri.authority, rawIri.path)
    rawIri.copy(authority = normalizedAuthority, path = normalizedPath)
  }
}
