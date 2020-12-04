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

import fr.davit.capturl.scaladsl.Host.{IPv4Host, IPv6Host}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HostSpec extends AnyFlatSpec with Matchers {

  "IPv4Host" should "normalize ip address" in {
    IPv4Host("127.000.000.001").toString shouldBe "127.0.0.1"
  }

  "IPv6Host" should "normalize ip address" in {
    IPv6Host("2001:0db8:0000:0000:0000:ff00:0042:8329").toString shouldBe "2001:db8::ff00:42:8329"
    IPv6Host("2001:0:0:1:0:0:0:1").toString shouldBe "2001:0:0:1::1"
    IPv6Host("fec0:0:0:0:0:0:0:0").toString shouldBe "fec0::"
    IPv6Host("0:0:0:0:0:0:0:1").toString shouldBe "::1"
  }
}
