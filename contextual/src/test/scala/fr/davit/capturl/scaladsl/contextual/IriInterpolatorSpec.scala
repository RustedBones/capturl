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

package fr.davit.capturl.scaladsl.contextual

import fr.davit.capturl.scaladsl.Authority.Port
import fr.davit.capturl.scaladsl.Path.{Segment, Slash}
import fr.davit.capturl.scaladsl._
import fr.davit.capturl.scaladsl.contextual.iri._
import org.scalatest.{FlatSpec, Matchers}

class IriInterpolatorSpec extends FlatSpec with Matchers {

  val scheme    = Scheme.Protocol("http")
  val authority = Authority(Host.NamedHost("localhost"), Port.Number(8080))
  val path      = Slash(Segment("path"))
  val query     = Query.Part("key", None, Query.Empty)
  val fragment  = Fragment.Identifier("identifier")

  "IriInterpolator" should "interpolate Iri from string" in {
    iri"http://localhost:8080/path?key#identifier" shouldBe StrictIri(scheme, authority, path, query, fragment)
  }

  it should "not compile when interpolating invalid iris" in {
    """ val myIri = iri"http://user{info@example.com/" """ shouldNot compile
  }
}
