package fr.davit.capturl

import fr.davit.capturl.scaladsl.Authority.Port
import fr.davit.capturl.scaladsl._
import org.scalatest.{FlatSpec, Matchers}

class IriSpec extends FlatSpec with Matchers {

  val testHost = Host.NamedHost("example.com")

  "iri" should "tell if it is absolute" in {
    Iri().isAbsolute shouldBe false
    Iri(scheme = Scheme.HTTP).isAbsolute shouldBe true
    Iri(authority = Authority(testHost)).isAbsolute shouldBe false
    Iri(path = Path.root).isAbsolute shouldBe false
    Iri(query = Query.Part("key")).isAbsolute shouldBe false
    Iri(fragment = Fragment.Identifier("identifier")).isAbsolute shouldBe false
  }

  it should "normalize port" in {
    val port = 8080
    val scheme = Scheme("test", port)
    val authority = Authority(testHost)
    val authorityWithPort = authority.copy(port = Port.Number(port))

    Iri(scheme, authorityWithPort) shouldBe Iri(scheme, authority)
  }

  it should "normalize path" in {
    Iri(Scheme.File) shouldBe Iri(Scheme.File, path = Path.root)
    Iri(authority = Authority(testHost)) shouldBe Iri(authority = Authority(testHost), path = Path.root)
  }

  it should "resolve iris" in {
    val base = Iri(
      Scheme.HTTP,
      Authority(testHost),
      Path.Resource("directory/file"),
      Query.Part("key"),
      Fragment.Identifier("identifier")
    )

    val otherScheme = Scheme.HTTPS
    val otherAuthority = Authority(Host.NamedHost("other.com"))
    val otherAbsolutePath = Path.Resource("otherDirectory/otherFile")
    val otherRelativePath = Path.Resource("otherFile")
    val otherQuery = Query.Part("otherKey")
    val otherFragment = Fragment.Identifier("otherIdentifier")

    {
      val otherIri = Iri(
        scheme = otherScheme,
        authority = otherAuthority,
        path = otherAbsolutePath,
        query = otherQuery,
        fragment = otherFragment
      )
      base.resolve(otherIri) shouldBe otherIri
    }

    {
      val otherIri = Iri(
        authority = otherAuthority,
        path = otherAbsolutePath,
        query = otherQuery,
        fragment = otherFragment
      )
      base.resolve(otherIri) shouldBe otherIri.withScheme(base.scheme)
    }

    {
      val otherIri = Iri(
        path = otherAbsolutePath,
        query = otherQuery,
        fragment = otherFragment
      )
      base.resolve(otherIri) shouldBe otherIri
        .withScheme(base.scheme)
        .withAuthority(base.authority)
    }

    {
      val otherIri = Iri(
        path = otherRelativePath,
        query = otherQuery,
        fragment = otherFragment
      )
      base.resolve(otherIri) shouldBe otherIri
        .withScheme(base.scheme)
        .withAuthority(base.authority)
        .withPath(base.path.resolve(otherIri.path))
    }

    {
      val otherIri = Iri(
        query = otherQuery,
        fragment = otherFragment
      )
      base.resolve(otherIri) shouldBe otherIri
        .withScheme(base.scheme)
        .withAuthority(base.authority)
        .withPath(base.path)
    }

    {
      val otherIri = Iri(
        fragment = otherFragment
      )
      base.resolve(otherIri) shouldBe otherIri
        .withScheme(base.scheme)
        .withAuthority(base.authority)
        .withPath(base.path)
        .withQuery(base.query)
    }

  }

}
