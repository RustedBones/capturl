package fr.davit.capturl.scaladsl

import java.util.Objects

import fr.davit.capturl.javadsl
import fr.davit.capturl.parsers.IriParser
import org.parboiled2.Parser.DeliveryScheme.Throw

final case class Iri(
    scheme: Scheme = Scheme.empty,
    authority: Authority = Authority.empty,
    path: Path = Path.empty,
    query: Query = Query.empty,
    fragment: Fragment = Fragment.empty
) extends javadsl.Iri {

  private lazy val normalizedAuthority: Authority = {
    if (Scheme.defaultPort(scheme).contains(authority.port)) authority.withPort(Authority.Port.empty)
    else authority
  }

  private lazy val normalizedPath: Path = {
    if (scheme.nonEmpty || authority.nonEmpty) Path.root.resolve(path) else path
  }

  def isAbsolute: Boolean = scheme.nonEmpty
  def isRelative: Boolean = !isAbsolute

  def withScheme(scheme: Scheme): Iri = copy(scheme =  scheme)
  override def withScheme(scheme: String): Iri = withScheme(Scheme(scheme))

  def withAuthority(authority: Authority): Iri = copy(authority =  authority)
  override def withAuthority(authority: String): Iri = withAuthority(Authority(authority))

  def withPath(path: Path): Iri = copy(path = path)
  override def withPath(path: String): Iri = withPath(Path(path))

  def withQuery(query: Query): Iri = copy(query = query)
  override def withQuery(query: String): Iri = withQuery(Query(query))

  def withFragment(fragment: Fragment): Iri = copy(fragment = fragment)
  override def withFragment(fragment: String): Iri = withFragment(Fragment(fragment))

  def relativize(iri: Iri): Iri = iri match {
    case Iri(s, a, p, q, f) =>
      if ((s.nonEmpty && s != scheme) ||
          (a.nonEmpty && iri.normalizedAuthority != normalizedAuthority)) {
        iri
      } else {
       Iri(path = path.relativize(p), query = q, fragment = f)
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

  def empty = Iri()

  def apply(iri: String): Iri = {
    IriParser(iri).IRI.run()
  }
}
