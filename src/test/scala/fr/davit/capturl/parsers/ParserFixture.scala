package fr.davit.capturl.parsers

import org.parboiled2._
import org.scalatest.Assertions

object ParserFixture {

  val DebugFormatter = new ErrorFormatter(showTraces = true)

  abstract class TestParser[T](override val input: ParserInput) extends Parser with StringBuilding {
    def rule: Rule1[T]
  }

}

trait ParserFixture[T] {

  import Parser.DeliveryScheme.Throw
  import ParserFixture._

  def createParser(input: ParserInput): TestParser[T]

  def parse(data: String, canThrow: Boolean = false): (T, String) = {
    val parser = createParser(data)

    try {
      val result = parser.rule.run()
      val rest = data.drop(parser.cursor)
      result -> rest
    } catch {
      case e: ParseError if !canThrow => Assertions.fail(parser.formatError(e, DebugFormatter))
    }
  }


}
