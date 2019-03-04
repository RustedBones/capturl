package fr.davit.capturl.parsers

import fr.davit.capturl.Query
import org.parboiled2.{CharPredicate, Parser, Rule1}

object QueryParser {
  def apply(path: String): Parser with QueryParser = {
    new StringParser(path) with QueryParser
  }
}

trait QueryParser extends RichStringBuilding { this: Parser =>

  def iquery: Rule1[Query] = rule {
    clearSB() ~ (ipchar | iprivate | CharPredicate('/', '?') ~ appendSB()).* ~ push(new Query(sb.toString))
  }



}
