package fr.davit.capturl

import fr.davit.capturl.scaladsl.Host.{IPv4Host, IPv6Host}
import org.scalatest.{FlatSpec, Matchers}

class HostSpec extends FlatSpec with Matchers {

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
