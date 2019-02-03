package fr.davit.capturl.parsers

import org.parboiled2.CharPredicate._
import org.parboiled2._

trait RichStringBuilding { this: Parser with StringBuilding =>

  def `sub-delims` = rule {
    CharPredicate('!', '$', '&', '\'', '(', ')', '*', '+', ',', ';', '=') ~ appendSB()
  }

  private val unreservedCodePoints = Seq(
    0x00A0 to 0xD7FF, 0xF900 to 0xFDCF, 0xFDF0 to 0xFFEF,
      0x10000 to 0x1FFFD, 0x20000 to 0x2FFFD, 0x30000 to 0x3FFFD,
      0x40000 to 0x4FFFD, 0x50000 to 0x5FFFD, 0x60000 to 0x6FFFD,
      0x70000 to 0x7FFFD, 0x80000 to 0x8FFFD, 0x90000 to 0x9FFFD,
      0xA0000 to 0xAFFFD, 0xB0000 to 0xBFFFD, 0xC0000 to 0xCFFFD,
      0xD0000 to 0xDFFFD, 0xE1000 to 0xEFFFD
  )

  def ucschar: Rule0 = rule {
    // support of unicode
    capture((ANY ~ test(Character.isHighSurrogate(lastChar)) ~ ANY ~ test(Character.isLowSurrogate(lastChar))) | ANY) ~> {
      chars: String => test(unreservedCodePoints.exists(_.contains(Character.codePointAt(chars, 0)))) ~ appendSB(chars)
    }
  }

  def iunreserved: Rule0 = rule {
    (AlphaNum ++ '-' ++ '.' ++ '_' ++ '~') ~ appendSB() | ucschar
  }

  def `pct-encoded`: Rule0 = rule {
    '%' ~ capture(HexDigit ~ HexDigit) ~> { hex: String =>
      val decoded = (java.lang.Short.valueOf(hex, 16): Short).toChar
      appendSB(decoded)
    }
  }

  def appendLowered(): Rule0 = rule {
    run(sb.append(CharUtils.toLowerCase(lastChar)))
  }

}
