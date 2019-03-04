package fr.davit.capturl.parsers

import fr.davit.capturl.Fragment
import org.parboiled2.{CharPredicate, Parser, Rule1}

object FragmentParser {
  def apply(fragment: String): Parser with FragmentParser = {
    new StringParser(fragment) with FragmentParser
  }
}

trait FragmentParser extends RichStringBuilding { this: Parser =>

  def ifragment: Rule1[Fragment] = rule {
    clearSB() ~ (ipchar | CharPredicate('/', '?') ~ appendSB()).* ~ push(new Fragment(sb.toString))
  }

}
