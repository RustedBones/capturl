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

package fr.davit.capturl.parsers

import fr.davit.capturl.parsers.ParserFixture.TestParser
import fr.davit.capturl.scaladsl.Authority.Port
import fr.davit.capturl.scaladsl.Path.{Segment, Slash}
import fr.davit.capturl.scaladsl._
import org.parboiled2.{ParserInput, Rule1}
import org.scalatest.{FlatSpec, Matchers}

class IriParserSpec extends FlatSpec with Matchers {

  class IriParserBehaviour(testRule: IriParser => Rule1[Iri]) {

    trait Fixture extends ParserFixture[Iri] {
      override def createParser(input: ParserInput) = new TestParser[Iri](input) with IriParser {
        override def rule = rule(testRule(this) ~ EOI)
      }
    }

    it should "parse absolute IRI" in new Fixture {
      val (iri, _) = parse("http://example.com/path?query#fragment")
      iri.scheme shouldBe Scheme.HTTP
      iri.authority shouldBe Authority(Host.NamedHost("example.com"))
      iri.path shouldBe Slash(Segment("path"))
      iri.query shouldBe Query.Part("query", None, Query.Empty)
      iri.fragment shouldBe Fragment.Identifier("fragment")
    }

    it should "parse scheme relative IRI" in new Fixture {
      val (iri, _) = parse("//example.com/path?query#fragment")
      iri.scheme shouldBe Scheme.empty
      iri.authority shouldBe Authority(Host.NamedHost("example.com"))
      iri.path shouldBe Slash(Segment("path"))
      iri.query shouldBe Query.Part("query", None, Query.Empty)
      iri.fragment shouldBe Fragment.Identifier("fragment")
    }

    it should "parse host relative IRI" in new Fixture {
      val (iri, _) = parse("/path?query#fragment")
      iri.scheme shouldBe Scheme.empty
      iri.authority shouldBe Authority.empty
      iri.path shouldBe Slash(Segment("path"))
      iri.query shouldBe Query.Part("query", None, Query.Empty)
      iri.fragment shouldBe Fragment.Identifier("fragment")
    }

    it should "parse path relative IRI" in new Fixture {
      val (iri, _) = parse("?query#fragment")
      iri.scheme shouldBe Scheme.empty
      iri.authority shouldBe Authority.empty
      iri.path shouldBe Path.empty
      iri.query shouldBe Query.Part("query", None, Query.Empty)
      iri.fragment shouldBe Fragment.Identifier("fragment")
    }

    it should "parse query relative IRI" in new Fixture {
      val (iri, _) = parse("#fragment")
      iri.scheme shouldBe Scheme.empty
      iri.authority shouldBe Authority.empty
      iri.path shouldBe Path.empty
      iri.query shouldBe Query.empty
      iri.fragment shouldBe Fragment.Identifier("fragment")
    }

    it should "normalize autority port" in new Fixture {
      val (iri, _) = parse("http://example.com:80")
      iri.authority shouldBe Authority(Host.NamedHost("example.com"), Port.Number(80))
      iri.normalizedAuthority shouldBe Authority(Host.NamedHost("example.com"))
    }

    it should "normalize empty path when host/scheme defined" in new Fixture {
      val (iri, _) = parse("http://example.com")
      iri.path shouldBe Path.empty
      iri.normalizedPath shouldBe Path.root
    }
  }

  "StrictIriParser" should behave like new IriParserBehaviour(_.IRI) {
    it should "reject invalid IRIs" in new Fixture {
      parseError("http://user{info@example.com/") shouldBe """Invalid input "user{i", expected iauthority, ipath-abempty, '?', '#' or 'EOI' (line 1, column 8):
                                                             |http://user{info@example.com/
                                                             |       ^""".stripMargin

      parseError("http://example.com:-1/") shouldBe """Invalid input "-1/", expected iuserinfo, '@', ireg-name or port (line 1, column 20):
                                                      |http://example.com:-1/
                                                      |                   ^""".stripMargin
      //    TODO fail on strict
      //    parseError("http://example.com/path with space") shouldBe """Invalid input " w", expected absolute or empty path, query, fragment or 'EOI' (line 1, column 24):
      //                                                                |http://example.com/path with space
      //                                                                |                       ^""".stripMargin
    }
  }

  "LazyIriParser" should behave like new IriParserBehaviour(_.IRILazy)

}
