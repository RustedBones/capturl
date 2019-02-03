package fr.davit.capturl.parsers

import fr.davit.capturl.Scheme
import org.parboiled2.CharPredicate._
import org.parboiled2.{Parser, Rule1, StringBuilding}

object SchemeParser {
  private val SchemeChars = AlphaNum ++ '+' ++ '-' ++ '.'
}

trait SchemeParser extends RichStringBuilding {
  this: Parser with StringBuilding =>

  import SchemeParser._

  def scheme: Rule1[Scheme] = rule {
    clearSB() ~
      Alpha ~ appendLowered() ~ (SchemeChars ~ appendLowered()).* ~ &(':') ~
      push(Scheme(sb.toString))
  }
}
