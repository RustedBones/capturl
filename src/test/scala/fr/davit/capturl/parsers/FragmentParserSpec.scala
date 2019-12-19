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
import fr.davit.capturl.scaladsl.{Fragment, Path}
import org.parboiled2.ParserInput
import org.scalatest.{FlatSpec, Matchers}

class FragmentParserSpec extends FlatSpec with Matchers {

  trait Fixture extends ParserFixture[Fragment] {
    override def createParser(input: ParserInput) = new TestParser[Fragment](input) with FragmentParser {
      override def rule = ifragment
    }
  }

  "FragmentParser" should "parse fragment" in new Fixture {
    parse("") shouldBe Fragment.Identifier("")                     -> ""
    parse("identifier") shouldBe Fragment.Identifier("identifier") -> ""
    parse("%C3%B6") shouldBe Fragment.Identifier("ö")              -> ""

    // relax parsing
    parse("fragment with spaces") shouldBe Fragment.Identifier("fragment with spaces") -> ""
  }

}
