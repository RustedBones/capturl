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
import java.net.IDN

import fr.davit.capturl.scaladsl.Host
import fr.davit.capturl.scaladsl.Host.{IPv4Host, IPv6Host, NamedHost}
import org.parboiled2.CharPredicate._
import org.parboiled2.Rule1

object HostParser {

  def apply(host: String): StringParser with HostParser = {
    new StringParser(host) with HostParser
  }
}

trait HostParser extends RichStringBuilding {
  this: StringParser =>

  //--------------------------------------------------------------------------------------------------------------------
  // IPv4
  //--------------------------------------------------------------------------------------------------------------------
  // relax parsing on leading zeros compared to RFC
  // https://www.ietf.org/rfc/rfc3986.txt
  def `dec-octet`: Rule1[Byte] = rule {
    capture((1 to 3).times(Digit)) ~> { octet: String =>
      val value = octet.toInt.toByte // mut go to int 1st because string value is unsigned
      test(value < 256) ~ push(value)
    }
  }

  def IPv4address: Rule1[IPv4Host] = rule {
    atomic {
      4.times(`dec-octet`).separatedBy('.') ~> ((bs: Seq[Byte]) => IPv4Host(bs.toList))
    }
  }

  //--------------------------------------------------------------------------------------------------------------------
  // IPv6
  //--------------------------------------------------------------------------------------------------------------------
  def empty: Rule1[Seq[Byte]] = rule {
    push(Seq.empty[Byte])
  }

  def h16: Rule1[Seq[Byte]] = rule {
    capture((1 to 4).times(HexDigit)) ~> { hex: String =>
      val decoded = Integer.parseUnsignedInt(hex, 16)
      Seq((decoded >> 8).toByte, (decoded & 0xFF).toByte)
    }
  }

  def ls32: Rule1[Seq[Byte]] = rule {
    2.times(h16).separatedBy(':') ~> ((bs: Seq[Seq[Byte]]) => bs.flatten) | IPv4address ~> (
        (ipv4: IPv4Host) => ipv4.bytes
    )
  }

  def hextets(max: Int): Rule1[Seq[Byte]] = {
    if (max <= 0) empty
    else rule((1 to max).times(h16).separatedBy(':') ~> ((bs: Seq[Seq[Byte]]) => bs.flatten) | empty)
  }

  def IPv6address: Rule1[IPv6Host] = {
    def onePart: Rule1[Seq[Byte]] = rule {
      8.times(h16).separatedBy(':') ~> ((bs: Seq[Seq[Byte]]) => bs.flatten)
    }

    def highPart: Rule1[Seq[Byte]] = rule {
      hextets(7)
    }

    def lowPart(max: Int): Rule1[Seq[Byte]] = {
      if (max < 2) rule(h16 | empty)
      if (max == 2) rule(ls32 | h16 | empty)
      else rule((hextets(max - 2) ~ ':' ~ ls32) ~> ((a: Seq[Byte], b: Seq[Byte]) => a ++ b) | ls32 | h16 | empty)
    }

    def splitted: Rule1[Seq[Byte]] = rule {
      (highPart ~ "::") ~> { high: Seq[Byte] =>
        lowPart(7 - high.size) ~> { low: Seq[Byte] =>
          val padding = List.fill[Byte](16 - (high.size + low.size))(0)
          high ++ padding ++ low
        }
      }
    }

    rule {
      atomic {
        (onePart | splitted) ~> ((bs: Seq[Byte]) => IPv6Host(bs.toList))
      }
    }
  }

  def `IP-literal`: Rule1[IPv6Host] = rule {
    '[' ~ IPv6address ~ ']' // TODO IPvFuture ?
  }

  def `ireg-name`: Rule1[Host] = rule {
    atomic {
      clearSB() ~ (iunreserved | `pct-encoded` | `sub-delims`).* ~
        push {
          val hostname = sb.toString
          if (hostname.isEmpty) Host.Empty else NamedHost(IDN.toUnicode(hostname).toLowerCase)
        }
    }
  }

  def ihost: Rule1[Host] = rule {
    `IP-literal` | IPv4address | `ireg-name`
  }

}
