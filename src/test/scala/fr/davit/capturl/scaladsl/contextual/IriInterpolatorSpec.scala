package fr.davit.capturl.scaladsl.contextual

import fr.davit.capturl.scaladsl.Authority.Port
import fr.davit.capturl.scaladsl._
import org.scalatest.{FlatSpec, Matchers}
import fr.davit.capturl.scaladsl.contextual.iri._

class IriInterpolatorSpec extends FlatSpec with Matchers {


  val scheme = Scheme.Protocol("http")
  val authority = Authority(Host.NamedHost("localhost"), Port.Number(8080))
  val path = Path.Slash(Path.Segment("path", Path.End))
  val query = Query.Part("key", None, Query.Empty)
  val fragment = Fragment.Identifier("identifier")

  "IriInterpolator" should "interpolate Iri from string" in {
    iri"http://localhost:8080/path?key" shouldBe Iri(scheme, authority, path, query, fragment)
  }

  it should "interpolate Iri from classes" in {
    iri"$scheme://$authority/$path?$query#$fragment" shouldBe Iri(scheme, authority, path, query, fragment)
  }

}
