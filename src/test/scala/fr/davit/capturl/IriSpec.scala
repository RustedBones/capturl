package fr.davit.capturl

import fr.davit.capturl.scaladsl.Authority.Port
import fr.davit.capturl.scaladsl.Iri.ParsingMode.{Lazy, Strict}
import fr.davit.capturl.scaladsl.Path.{Segment, Slash}
import fr.davit.capturl.scaladsl._
import org.parboiled2.ParseError
import org.scalatest.{FlatSpec, Matchers}

class IriSpec extends FlatSpec with Matchers {

  val testHost = Host.NamedHost("example.com")

  "StrictIri" should "tell if it is absolute" in {
    StrictIri().isAbsolute shouldBe false
    StrictIri(scheme = Scheme.HTTP).isAbsolute shouldBe true
    StrictIri(authority = Authority(testHost)).isAbsolute shouldBe false
    StrictIri(path = Path.root).isAbsolute shouldBe false
    StrictIri(query = Query.Part("key")).isAbsolute shouldBe false
    StrictIri(fragment = Fragment.Identifier("identifier")).isAbsolute shouldBe false
  }

  it should "normalize port" in {
    val port = 8080
    val scheme = Scheme("test", port)
    val authority = Authority(testHost)
    val authorityWithPort = authority.copy(port = Port.Number(port))

    StrictIri(scheme, authorityWithPort) shouldBe StrictIri(scheme, authority)
  }

  it should "normalize path" in {
    StrictIri(Scheme.File, path = Segment("..")) shouldBe StrictIri(Scheme.File, path = Path.root)
    StrictIri(Scheme.File) shouldBe StrictIri(Scheme.File, path = Path.root)
    StrictIri(authority = Authority(testHost)) shouldBe StrictIri(authority = Authority(testHost), path = Path.root)
    StrictIri(Scheme.File, path = Segment("file")) shouldBe StrictIri(Scheme.File, path = Slash(Segment("file")))
  }

  it should "resolve iris" in {
    val base = StrictIri(
      Scheme.HTTP,
      Authority(testHost),
      Segment("directory", Slash(Segment("file"))),
      Query.Part("key"),
      Fragment.Identifier("identifier")
    )

    val otherScheme = Scheme.HTTPS
    val otherAuthority = Authority(Host.NamedHost("other.com"))
    val otherAbsolutePath = Slash(Segment("otherDirectory", Slash(Segment("otherFile"))))
    val otherRelativePath = Segment("otherFile")
    val otherQuery = Query.Part("otherKey")
    val otherFragment = Fragment.Identifier("otherIdentifier")

    {
      val otherIri = StrictIri(
        scheme = otherScheme,
        authority = otherAuthority,
        path = otherAbsolutePath,
        query = otherQuery,
        fragment = otherFragment
      )
      base.resolve(otherIri) shouldBe otherIri
    }

    {
      val otherIri = StrictIri(
        authority = otherAuthority,
        path = otherAbsolutePath,
        query = otherQuery,
        fragment = otherFragment
      )
      base.resolve(otherIri) shouldBe otherIri.withScheme(base.scheme)
    }

    {
      val otherIri = StrictIri(
        path = otherAbsolutePath,
        query = otherQuery,
        fragment = otherFragment
      )
      base.resolve(otherIri) shouldBe otherIri
        .withScheme(base.scheme)
        .withAuthority(base.authority)
    }

    {
      val otherIri = StrictIri(
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
      val otherIri = StrictIri(
        query = otherQuery,
        fragment = otherFragment
      )
      base.resolve(otherIri) shouldBe otherIri
        .withScheme(base.scheme)
        .withAuthority(base.authority)
        .withPath(base.path)
    }

    {
      val otherIri = StrictIri(
        fragment = otherFragment
      )
      base.resolve(otherIri) shouldBe otherIri
        .withScheme(base.scheme)
        .withAuthority(base.authority)
        .withPath(base.path)
    }

  }

  "LazyIri" should "parse more than StrictIri parsers" in {
    val url = "http://user{info@example.com/"
    a[ParseError] shouldBe thrownBy(Iri(url, Strict))
    noException shouldBe thrownBy(Iri(url, Lazy))
  }

  it should "accept empty queries" in {
    Iri("path?", Lazy) shouldBe Iri("path?", Strict)
    Iri("path?#fragment", Lazy) shouldBe Iri("path?#fragment", Strict)
  }

  it should "accept empty fragment" in {
    Iri("path#", Lazy) shouldBe Iri("path#", Strict)
    Iri("path#", Lazy) shouldBe Iri("path#", Strict)
    Iri("path?#", Lazy) shouldBe Iri("path?#", Strict)
  }

}
