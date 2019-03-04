package fr.davit.capturl

final case class Iri(scheme: Scheme, authority: Authority, path: Path, query: Query, fragment: Fragment)

object Iri {

}
