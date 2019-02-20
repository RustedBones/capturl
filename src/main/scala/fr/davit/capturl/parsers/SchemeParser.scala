package fr.davit.capturl.parsers

import fr.davit.capturl.Scheme
import org.parboiled2.CharPredicate._
import org.parboiled2.{Parser, Rule1}

object SchemeParser {
  private val SchemeChars = AlphaNum ++ '+' ++ '-' ++ '.'

  def apply(scheme: String): SchemeParser = new StringParser(scheme) with SchemeParser
}

trait SchemeParser extends RichStringBuilding { this: Parser =>

  import SchemeParser._

  def scheme: Rule1[Scheme] = rule {
    clearSB() ~
      Alpha ~ appendLowered() ~ (SchemeChars ~ appendLowered()).* ~ &(':') ~
      push(new Scheme(sb.toString))
  }
}
