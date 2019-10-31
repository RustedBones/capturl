package fr.davit.capturl.parsers

import fr.davit.capturl.parsers.StringParser.ParseException
import org.parboiled2.Parser.DeliveryScheme
import org.parboiled2.{ParseError, Parser, ParserInput, Rule1}

import scala.util.{Failure, Try}

object StringParser {
  class ParseException(input: ParserInput, error: ParseError) extends RuntimeException(error.format(input))
}

class StringParser(override val input: ParserInput) extends Parser {

  def phrase[T](r: this.type => Rule1[T]): Try[T] = {
    __run(rule(r(this) ~ EOI))(DeliveryScheme.Try) match {
      case Failure(e: ParseError) => Failure(new ParseException(input, e))
      case result                 => result
    }
  }

}
