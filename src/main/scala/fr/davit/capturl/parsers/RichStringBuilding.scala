package fr.davit.capturl.parsers

import org.parboiled2.CharPredicate._
import org.parboiled2._
import shapeless._

trait RichStringBuilding extends StringBuilding { this: Parser =>

  def phrase[T](r: this.type  => Rule1[T], name: String)(implicit scheme: Parser.DeliveryScheme[T :: HNil]): scheme.Result = {
    __run(rule(r(this).named(name) ~ EOI))
  }

  protected lazy val `sub-delims-predicate` = CharPredicate('!', '$', '&', '\'', '(', ')', '*', '+', ',', ';', '=')

  protected def `sub-delims` = rule {
    `sub-delims-predicate` ~ appendSB()
  }

  protected def codePointInRanges(ranges: Seq[Range]): Rule0 = rule {
    // support of unicode
    capture((ANY ~ test(Character.isHighSurrogate(lastChar)) ~ ANY ~ test(Character.isLowSurrogate(lastChar))) | ANY) ~> {
      chars: String => test(ranges.exists(_.contains(Character.codePointAt(chars, 0)))) ~ appendSB(chars)
    }
  }

  protected lazy val unreservedCodePoints = Seq(
    0x00A0 to 0xD7FF, 0xF900 to 0xFDCF, 0xFDF0 to 0xFFEF,
      0x10000 to 0x1FFFD, 0x20000 to 0x2FFFD, 0x30000 to 0x3FFFD,
      0x40000 to 0x4FFFD, 0x50000 to 0x5FFFD, 0x60000 to 0x6FFFD,
      0x70000 to 0x7FFFD, 0x80000 to 0x8FFFD, 0x90000 to 0x9FFFD,
      0xA0000 to 0xAFFFD, 0xB0000 to 0xBFFFD, 0xC0000 to 0xCFFFD,
      0xD0000 to 0xDFFFD, 0xE1000 to 0xEFFFD
  )

  protected def ucschar: Rule0 = rule {
    codePointInRanges(unreservedCodePoints)
  }

  protected lazy val `iunreserved-predicate` = AlphaNum ++ '-' ++ '.' ++ '_' ++ '~'

  protected def iunreserved: Rule0 = rule {
    `iunreserved-predicate` ~ appendSB() | ucschar
  }

  protected def `pct-encoded`: Rule0 = rule {
    ('%' ~ capture(HexDigit ~ HexDigit)).+ ~> { hexes: Seq[String] =>
      val bytes = hexes.map(hex => Integer.parseInt(hex, 16).toByte)
      appendSB(new String(bytes.toArray))
    }
  }

  protected def ipchar: Rule0 = rule {
    // TODO allow space only in 'relax' mode
    iunreserved | `pct-encoded` | `sub-delims` | CharPredicate(':', '@', ' ') ~ appendSB()
  }

  protected lazy val privateCodePoints = Seq(
    0xE000 to 0xF8FF, 0xF0000 to 0xFFFFD,  0x100000 to 0x10FFFD
  )

  protected def iprivate: Rule0 = rule {
    codePointInRanges(privateCodePoints)
  }

  //--------------------------------------------------------------------------------------------------------------------
  // String building
  //--------------------------------------------------------------------------------------------------------------------
  protected def appendLowered(): Rule0 = rule {
    run(sb.append(CharUtils.toLowerCase(lastChar)))
  }

  def phraseSB(r: this.type => Rule0)(implicit scheme: Parser.DeliveryScheme[String :: HNil]): scheme.Result = {
    __run(rule(r(this) ~ EOI ~ push(sb.toString)))
  }
}
