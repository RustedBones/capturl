package fr.davit.capturl.parsers

import org.parboiled2.{CharPredicate, Parser}

class FragmentParser extends RichStringBuilding { this: Parser =>

  def ifragment = rule {
    (ipchar | CharPredicate('/', '?') ~ appendSB()).*
  }

}
