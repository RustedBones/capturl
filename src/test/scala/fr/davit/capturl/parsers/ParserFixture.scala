package fr.davit.capturl.parsers

import org.parboiled2._
import org.scalatest.Assertions

object ParserFixture {

  val DebugFormatter = new ErrorFormatter(showTraces = true)

  abstract class TestParser[T](input: ParserInput) extends StringParser(input) with StringBuilding {
    def rule: Rule1[T]
  }

}

trait ParserFixture[T] {

  import Parser.DeliveryScheme.Throw
  import ParserFixture._

  def createParser(input: ParserInput): TestParser[T]

  private def run(data: String): Either[ParseError, (T, String)] = {
    val parser = createParser(data)
    try {
      val result = parser.rule.run()
      val rest   = data.drop(parser.cursor)
      Right(result -> rest)
    } catch {
      case e: ParseError => Left(e)
    }
  }

  def parse(data: String): (T, String) = {
    run(data) match {
      case Right(success) => success
      case Left(error)    => Assertions.fail(DebugFormatter.format(error, data))
    }
  }

  def parseError(data: String): String = {
    run(data) match {
      case Left(error)    => error.format(data)
      case Right(success) => Assertions.fail(s"ParseError expected but got $success")
    }
  }

}
