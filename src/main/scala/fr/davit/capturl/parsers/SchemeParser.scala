package fr.davit.capturl.parsers
import fr.davit.capturl.scaladsl.Scheme
import fr.davit.capturl.scaladsl.Scheme.Protocol
import org.parboiled2.CharPredicate._
import org.parboiled2.{Parser, Rule1}

object SchemeParser {
  private val SchemeChars = AlphaNum ++ '+' ++ '-' ++ '.'

  def apply(scheme: String): Parser with SchemeParser = {
    new StringParser(scheme) with SchemeParser
  }
}

trait SchemeParser extends RichStringBuilding { this: Parser =>

  import SchemeParser._

  def scheme: Rule1[Scheme] = rule {
    clearSB() ~
      Alpha ~ appendLowered() ~ (SchemeChars ~ appendLowered()).* ~
      push(Protocol(sb.toString))
  }
}
