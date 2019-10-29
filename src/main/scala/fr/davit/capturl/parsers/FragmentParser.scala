package fr.davit.capturl.parsers

import fr.davit.capturl.scaladsl.Fragment
import fr.davit.capturl.scaladsl.Fragment.Identifier
import org.parboiled2.{CharPredicate, Rule1}

object FragmentParser {
  def apply(fragment: String): StringParser with FragmentParser = {
    new StringParser(fragment) with FragmentParser
  }
}

trait FragmentParser extends RichStringBuilding { this: StringParser =>

  def ifragment: Rule1[Fragment] = rule {
    clearSB() ~ (ipchar | CharPredicate('/', '?') ~ appendSB()).* ~ push(Identifier(sb.toString))
  }

}
