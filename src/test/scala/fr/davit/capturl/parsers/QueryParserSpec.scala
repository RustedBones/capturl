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
import fr.davit.capturl.scaladsl.Query
import org.parboiled2.ParserInput
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class QueryParserSpec extends AnyFlatSpec with Matchers {

  trait Fixture extends ParserFixture[Query] {
    override def createParser(input: ParserInput) = new TestParser[Query](input) with QueryParser {
      override def rule = iquery
    }
  }

  "QueryParser" should "parse query" in new Fixture {
    parse("#fragment") shouldBe Query.Part("")                                                     -> "#fragment"
    parse("key1=val1&key2#fragment") shouldBe Query.Part("key1", Some("val1"), Query.Part("key2")) -> "#fragment"
    parse("%C3%B6=val1#fragment") shouldBe Query.Part("ö", Some("val1"))                           -> "#fragment"

    // relax parsing
    parse("key with+spaces#fragment") shouldBe Query.Part("key with spaces") -> "#fragment"
  }

}
