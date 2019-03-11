package fr.davit.capturl.scaladsl

final case class Iri private [capturl] (scheme: Scheme, authority: Authority, path: Path, query: Query, fragment: Fragment) {
  def isAbsolute: Boolean = scheme.nonEmpty
  def isRelative: Boolean = !isAbsolute

  override def toString: String = {
    val b = StringBuilder.newBuilder
    if (scheme.nonEmpty) b.append(s"$scheme://")
    if (authority.nonEmpty) b.append(authority)
    if (path.nonEmpty) b.append(path)
    if (query.nonEmpty) b.append(s"?$query")
    if (fragment.nonEmpty) b.append(s"#$fragment")
    b.toString
  }
}

object Iri {
  def apply(scheme: Scheme, authority: Authority, path: Path, query: Query, fragment: Fragment): Iri = {
    new Iri(scheme, authority.normalize(scheme), path, query, fragment)
  }
}
