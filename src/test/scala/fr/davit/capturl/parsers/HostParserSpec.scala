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
import fr.davit.capturl.scaladsl.Host
import fr.davit.capturl.scaladsl.Host.{IPv4Host, IPv6Host, NamedHost}
import org.parboiled2.ParserInput
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HostParserSpec extends AnyFlatSpec with Matchers {

  trait Fixture extends ParserFixture[Host] {
    override def createParser(input: ParserInput) = new TestParser[Host](input) with HostParser {
      override def rule = ihost
    }
  }

  "HostParser" should "parse IPv4 hosts" in new Fixture {
    parse("0.0.0.0:80") shouldBe IPv4Host(Vector(0, 0, 0, 0).map(_.toByte))                   -> ":80"
    parse("255.255.255.255/path") shouldBe IPv4Host(Vector(255, 255, 255, 255).map(_.toByte)) -> "/path"
    parse("09.09.09.09") shouldBe IPv4Host(Vector(9, 9, 9, 9).map(_.toByte))                  -> ""

    parse("256.256.256.256") should not be a[IPv4Host] // 256.256.256.256 is considered as a named host
  }

  it should "parse IPv6 hosts" in new Fixture {
    parse("[::1]:80") shouldBe IPv6Host(
      Vector(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1).map(_.toByte)
    ) -> ":80"
    parse("[2001:0db8:0000:0000:0000:ff00:0042:8329]/path") shouldBe IPv6Host(
      Vector(0x20, 0x01, 0x0d, 0xb8, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xff, 0x00, 0x00, 0x42, 0x83, 0x29)
        .map(_.toByte)
    ) -> "/path"

    parse("::1") should not be a[IPv4Host] // IPv6 is not in square braces
    parse("[::FG00]") should not be a[IPv4Host] // FG00 is not valid hexadecimal
  }

  it should "parse domains" in new Fixture {
    parse("") shouldBe Host.Empty                                -> ""
    parse("example.com:80") shouldBe NamedHost("example.com")    -> ":80"
    parse("bücher.example") shouldBe NamedHost("bücher.example") -> ""

    // normalization
    parse("Example.COM/path") shouldBe NamedHost("example.com") -> "/path"
    parse("ἀῼ") shouldBe NamedHost("ἀῳ")                        -> "" // lower case unicode extended
    parse("xn--bcher-kva.tld") shouldBe NamedHost("bücher.tld") -> "" // punycode to unicode
  }

}
