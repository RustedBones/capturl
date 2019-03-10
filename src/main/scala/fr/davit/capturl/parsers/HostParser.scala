package fr.davit.capturl.parsers
import fr.davit.capturl.scaladsl.Host.{IPv4Host, IPv6Host, NamedHost}
import fr.davit.capturl.scaladsl.Host
import org.parboiled2.CharPredicate._
import org.parboiled2.{Parser, Rule1}

object HostParser {
  def apply(host: String): Parser with HostParser = {
    new StringParser(host) with HostParser
  }
}

trait HostParser extends RichStringBuilding {
  this: Parser =>

  //--------------------------------------------------------------------------------------------------------------------
  // IPv4
  //--------------------------------------------------------------------------------------------------------------------
  // relax parsing on leading zeros compared to RFC
  // https://www.ietf.org/rfc/rfc3986.txt
  def `dec-octet`: Rule1[Int] = rule {
    capture((1 to 3).times(Digit)) ~> { octet: String =>
      val value = octet.toInt
      test(value < 256) ~ push(value)
    }
  }

  def IPv4address: Rule1[IPv4Host] = rule {
    4.times(`dec-octet`).separatedBy('.') ~> ((bs: Seq[Int]) => IPv4Host(bs.toList))
  }

  //--------------------------------------------------------------------------------------------------------------------
  // IPv6
  //--------------------------------------------------------------------------------------------------------------------
  def empty: Rule1[Seq[Int]] = rule {
    push(Seq.empty[Int])
  }

  def h16: Rule1[Seq[Int]] = rule {
    capture((1 to 4).times(HexDigit)) ~> { hex: String =>
      val decoded = Integer.parseUnsignedInt(hex, 16)
      Seq(decoded >> 8, decoded & 0xFF)
    }
  }

  def ls32: Rule1[Seq[Int]] = rule {
    2.times(h16).separatedBy(':') ~> ((bs: Seq[Seq[Int]]) => bs.flatten) | IPv4address ~> ((ipv4: IPv4Host) => ipv4.bytes)
  }

  def hextets(max: Int): Rule1[Seq[Int]] = {
    if (max <= 0) empty
    else rule((1 to max).times(h16).separatedBy(':') ~> ((bs: Seq[Seq[Int]]) => bs.flatten) | empty)
  }

  def IPv6address: Rule1[IPv6Host] = {
    def onePart: Rule1[Seq[Int]] = rule{
      8.times(h16).separatedBy(':')  ~> ((bs: Seq[Seq[Int]]) => bs.flatten)
    }

    def highPart: Rule1[Seq[Int]] = rule {
      hextets(7)
    }

    def lowPart(max: Int): Rule1[Seq[Int]] = {
      if (max < 2) rule(h16 | empty)
      if (max == 2) rule(ls32 | h16 | empty)
      else rule((hextets(max - 2) ~ ':' ~ ls32) ~> ((a: Seq[Int], b: Seq[Int]) => a ++ b) | ls32 | h16 | empty)
    }

    def splitted: Rule1[Seq[Int]] = rule {
      (highPart ~ "::") ~> { high: Seq[Int] =>
        lowPart(7 - high.size) ~> { low: Seq[Int] =>
          val padding = List.fill(16 - (high.size + low.size))(0)
          high ++ padding ++ low
        }
      }
    }

    rule((onePart | splitted) ~> ((bs: Seq[Int]) => IPv6Host(bs.toList)))
  }

  def `IP-literal`: Rule1[IPv6Host] = rule {
    '[' ~ IPv6address ~ ']' // TODO IPvFuture ?
  }

  def `ireg-name`: Rule1[NamedHost] = rule {
    clearSB() ~
      (iunreserved | `pct-encoded` | `sub-delims`).* ~
      push(new NamedHost(sb.toString.toLowerCase))
  }

  def ihost: Rule1[Host] = rule {
    `IP-literal` | IPv4address | `ireg-name`
  }

}
