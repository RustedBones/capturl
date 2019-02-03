package fr.davit.capturl

sealed abstract case class Iri(
    scheme: Scheme,
    authority: Authority,

)
