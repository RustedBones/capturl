package fr.davit.capturl.scaladsl.contextual

import fr.davit.capturl.scaladsl.Authority.Port
import fr.davit.capturl.scaladsl.Path.{Segment, Slash}
import fr.davit.capturl.scaladsl._
import fr.davit.capturl.scaladsl.contextual.iri._
import org.scalatest.{FlatSpec, Matchers}

class IriInterpolatorSpec extends FlatSpec with Matchers {


  val scheme = Scheme.Protocol("http")
  val authority = Authority(Host.NamedHost("localhost"), Port.Number(8080))
  val path = Slash(Segment("path"))
  val query = Query.Part("key", None, Query.Empty)
  val fragment = Fragment.Identifier("identifier")

  "IriInterpolator" should "interpolate Iri from string" in {
    iri"http://localhost:8080/path?key#identifier" shouldBe Iri(scheme, authority, path, query, fragment)
  }

  it should "not compile when interpolating invalid iris" in {
    """ val myIri = iri"http://user{info@example.com/" """ shouldNot compile
  }
}
