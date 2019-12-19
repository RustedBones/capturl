/*
 * Copyright 2019 Michel Davit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
