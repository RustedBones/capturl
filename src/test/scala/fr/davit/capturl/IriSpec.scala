/*
 * Copyright 2019 Michel Davit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.davit.capturl

import fr.davit.capturl.parsers.StringParser.ParseException
import fr.davit.capturl.scaladsl.Authority.Port
import fr.davit.capturl.scaladsl.Iri.ParsingMode.{Lazy, Strict}
import fr.davit.capturl.scaladsl.Path.{Segment, Slash}
import fr.davit.capturl.scaladsl._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class IriSpec extends AnyFlatSpec with Matchers {

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
    val port              = 8080
    val scheme            = Scheme("test", port)
    val authority         = Authority(testHost)
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

    val otherScheme       = Scheme.HTTPS
    val otherAuthority    = Authority(Host.NamedHost("other.com"))
    val otherAbsolutePath = Slash(Segment("otherDirectory", Slash(Segment("otherFile"))))
    val otherRelativePath = Segment("otherFile")
    val otherQuery        = Query.Part("otherKey")
    val otherFragment     = Fragment.Identifier("otherIdentifier")

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
    a[ParseException] shouldBe thrownBy(Iri(url, Strict))
    noException shouldBe thrownBy(Iri(url, Lazy))
  }

  it should "accept empty queries" in {
    Iri("path?", Lazy) shouldBe Iri("path?", Strict)
    Iri("path?#fragment", Lazy) shouldBe Iri("path?#fragment", Strict)
  }

  it should "accept empty fragment" in {
    Iri("path#", Lazy) shouldBe Iri("path#", Strict)
    Iri("path?#", Lazy) shouldBe Iri("path?#", Strict)
    Iri("path?query#", Lazy) shouldBe Iri("path?query#", Strict)
  }

  "Iri" should "ignore leading and trailing whitespace" in {
    Iri(" http://example.com/path\t", Lazy) shouldBe Iri("http://example.com/path")
    Iri(" http://example.com/path\t", Strict) shouldBe Iri("http://example.com/path")
    Iri(" http://example.com/path", Strict) shouldBe Iri("http://example.com/path")
    Iri("http://example.com/path ", Strict) shouldBe Iri("http://example.com/path")
    Iri("\nhttp://example.com/path") shouldBe Iri("http://example.com/path")
    Iri("http://example.com/p a   t h    ") shouldBe Iri("http://example.com/p a   t h")
    Iri("    http://example.com/path    ").scheme shouldBe Scheme.HTTP
    Iri("    http://example.com/path    ").path shouldBe Path("/path")
    a[ParseException] shouldBe thrownBy(Iri("\u00ADhttp://example.com/path")) // soft hyphen not counted as whitespace
  }

}
