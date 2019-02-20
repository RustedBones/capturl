package fr.davit.capturl.parsers

import org.parboiled2.{CharPredicate, Parser}

trait QueryParser extends RichStringBuilding { this: Parser =>

  def iquery = rule {
    ( ipchar | iprivate | CharPredicate('/', '?') ~ appendSB()).*
  }
}
