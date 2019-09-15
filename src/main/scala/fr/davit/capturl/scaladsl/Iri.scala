package fr.davit.capturl.scaladsl

import java.util.Objects

import fr.davit.capturl.javadsl
import fr.davit.capturl.parsers.IriParser
import fr.davit.capturl.scaladsl.Iri.RelativeIri
import org.parboiled2.Parser.DeliveryScheme.Throw

sealed abstract class Iri private[capturl] extends javadsl.Iri {
  def scheme: Scheme
  def authority: Authority
  def path: Path
  def query: Query
  def fragment: Fragment

  type Self <: Iri

  private lazy val normalizedAuthority: Authority = {
    if (Scheme.defaultPort(scheme).contains(authority.port)) authority.withPort(Authority.Port.empty)
    else authority
  }

  private lazy val normalizedPath: Path = {
    if ((scheme.nonEmpty || authority.nonEmpty) && path.isEmpty) Path.root
    else path
  }

  def isAbsolute: Boolean = scheme.nonEmpty
  def isRelative: Boolean = !isAbsolute

  def withScheme(scheme: Scheme): Iri
  override def withScheme(scheme: String): Iri = withScheme(Scheme(scheme))

  def withAuthority(authority: Authority): Self
  override def withAuthority(authority: String): Self = withAuthority(Authority(authority))

  def withPath(path: Path): Self
  override def withPath(path: String): Self = withPath(Path(path))

  def withQuery(query: Query): Self
  override def withQuery(query: String): Self = withQuery(Query(query))

  def withFragment(fragment: Fragment): Self
  override def withFragment(fragment: String): Self = withFragment(Fragment(fragment))

  def relativize(iri: Iri): Iri = iri match {
    case Iri(s, a, p, q, f) =>
      if ((s.nonEmpty && s != scheme) ||
          (a.nonEmpty && iri.normalizedAuthority != normalizedAuthority)) {
        iri
      } else {
        RelativeIri(Authority.empty, path.relativize(p), q, f)
      }
  }

  def resolve(iri: Iri): Iri = iri match {
    case Iri(s, a, p, q, f) =>
      if (s.nonEmpty) iri
      else if (a.nonEmpty) withAuthority(a).withPath(p).withQuery(q).withFragment(f)
      else if (p.nonEmpty) withPath(path.resolve(p)).withQuery(q).withFragment(f)
      else if (q.nonEmpty) withQuery(q).withFragment(f)
      else withFragment(f)
  }

  override def toString: String = {
    val b = new StringBuilder()
    if (scheme.nonEmpty) b.append(s"$scheme://")
    if (normalizedAuthority.nonEmpty) b.append(normalizedAuthority)
    if (normalizedPath.nonEmpty) b.append(normalizedPath)
    if (query.nonEmpty) b.append(s"?$query")
    if (fragment.nonEmpty) b.append(s"#$fragment")
    b.toString
  }

  /* Java API */
  override def getScheme: String                                        = scheme.toString
  override def getAuthority: javadsl.Authority                          = authority
  override def withAuthority(authority: javadsl.Authority): javadsl.Iri = withAuthority(authority.asScala)
  override def getPath: javadsl.Path                                    = path
  override def withPath(path: javadsl.Path): javadsl.Iri                = withPath(path.asScala)
  override def getQuery: javadsl.Query                                  = query
  override def withQuery(query: javadsl.Query): javadsl.Iri             = withQuery(query.asScala)
  override def getFragment: String                                      = fragment.toString
  override def relativize(iri: javadsl.Iri): javadsl.Iri                = relativize(iri.asScala)
  override def resolve(iri: javadsl.Iri): javadsl.Iri                   = resolve(iri.asScala)
  override def asScala(): Iri                                           = this

  override def equals(o: Any): Boolean = o match {
    case that: Iri =>
      Objects.equals(this.scheme, that.scheme) &&
        Objects.equals(this.normalizedAuthority, that.normalizedAuthority) &&
        Objects.equals(this.normalizedPath, that.normalizedPath) &&
        Objects.equals(this.query, that.query) &&
        Objects.equals(this.fragment, that.fragment)
    case _ => false
  }

  override def hashCode(): Int = Objects.hash(scheme, authority, path, query, fragment)

}

object Iri {

  case class AbsoluteIri(
      scheme: Scheme.Protocol,
      authority: Authority,
      path: Path,
      query: Query,
      fragment: Fragment
  ) extends Iri {

    override type Self = AbsoluteIri

    override def withScheme(scheme: Scheme): Iri = scheme match {
      case Scheme.Empty              => RelativeIri(authority, path, query, fragment)
      case protocol: Scheme.Protocol => copy(scheme = protocol)
    }

    override def withAuthority(authority: Authority): AbsoluteIri = copy(authority = authority)

    override def withPath(path: Path): AbsoluteIri = copy(path = path)

    override def withQuery(query: Query): AbsoluteIri = copy(query = query)

    override def withFragment(fragment: Fragment): AbsoluteIri = copy(fragment = fragment)
  }

  case class RelativeIri(
      authority: Authority,
      path: Path,
      query: Query,
      fragment: Fragment
  ) extends Iri {

    override type Self = RelativeIri

    override def scheme: Scheme = Scheme.empty

    override def withScheme(scheme: Scheme): Iri = scheme match {
      case Scheme.Empty              => this
      case protocol: Scheme.Protocol => AbsoluteIri(protocol, authority, path, query, fragment)
    }

    override def withAuthority(authority: Authority): RelativeIri = copy(authority = authority)

    override def withPath(path: Path): RelativeIri = copy(path = path)

    override def withQuery(query: Query): RelativeIri = copy(query = query)

    override def withFragment(fragment: Fragment): RelativeIri = copy(fragment = fragment)
  }

  def apply(
      scheme: Scheme = Scheme.empty,
      authority: Authority = Authority.empty,
      path: Path = Path.empty,
      query: Query = Query.empty,
      fragment: Fragment = Fragment.empty): Iri = {
    scheme match {
      case Scheme.Empty              => RelativeIri(authority, path, query, fragment)
      case protocol: Scheme.Protocol => AbsoluteIri(protocol, authority, path, query, fragment)
    }
  }

  def unapply(iri: Iri): Option[(Scheme, Authority, Path, Query, Fragment)] = {
    Some((iri.scheme, iri.authority, iri.path, iri.query, iri.fragment))
  }

  def apply(iri: String): Iri = {
    IriParser(iri).phrase(_.IRI)
  }
}
