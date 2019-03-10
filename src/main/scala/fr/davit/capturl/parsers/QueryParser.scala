package fr.davit.capturl.parsers
import fr.davit.capturl.scaladsl.Query
import org.parboiled2.{CharPredicate, Parser, Rule0, Rule1}

object QueryParser {
  def apply(path: String): Parser with QueryParser = {
    new StringParser(path) with QueryParser
  }
}

trait QueryParser extends RichStringBuilding { this: Parser =>

  protected lazy val `part-sub-delims-predicate` = {
    `sub-delims-predicate` -- CharPredicate('&', '+') ++ CharPredicate(':', '@', '/', '?')
  }


  def spaces: Rule0 = rule {
    '+' ~ appendSB(' ')
  }

  def `part-sub-delims`: Rule0 = rule {
    `part-sub-delims-predicate` ~ appendSB()
  }

  def `part-char`: Rule0 = rule {
    iunreserved | `pct-encoded` | spaces | `part-sub-delims`
  }

  def part: Rule1[String] = rule {
    clearSB() ~ `part-char`.* ~ push(sb.toString)
  }

  def iquery: Rule1[Query] = rule {
    part.*.separatedBy('&') ~> { parts: Seq[String] =>
      val b = Query.newBuilder
      parts.foreach { p =>
        val i = p.prefixLength(_ != '=')
        b += p.take(i) -> p.drop(i + 1)
      }
      b.result()
    }
  }
}
