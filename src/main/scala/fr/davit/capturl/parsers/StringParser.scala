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
