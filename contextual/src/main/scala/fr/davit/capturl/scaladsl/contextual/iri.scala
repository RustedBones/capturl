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

package fr.davit.capturl.scaladsl.contextual

import contextual._
import fr.davit.capturl.parsers.StringParser.ParseException
import fr.davit.capturl.scaladsl.Iri

object iri {

  object IriInterpolator extends Interpolator {

    override type Output = Iri

    override def contextualize(interpolation: StaticInterpolation): Seq[ContextType] = {
      interpolation.parts match {
        case (lit @ Literal(_, str)) :: Nil =>
          try Iri(str)
          catch {
            case e: ParseException => interpolation.abort(lit, 0, s"Invalid IRI $str\n${e.getMessage}")
          }
          Nil
        case parts =>
          val hole = parts.collectFirst { case h: Hole => h }.get
          interpolation.abort(hole, "iri: substitutions are not  yet supported")
      }
    }

    override def evaluator(
        contexts: Seq[ContextType],
        interpolation: StaticInterpolation
    ): interpolation.macroContext.Tree = {
      import interpolation.macroContext.universe._
      q"""IriInterpolator.evaluate(
        new IriInterpolator.RuntimeInterpolation(
          _root_.scala.collection.immutable.Seq(..${interpolation.literals}),
          _root_.scala.collection.immutable.Seq.empty
        )
      )"""
    }

    def evaluate(interpolation: RuntimeInterpolation): Iri = {
      Iri(interpolation.literals.mkString)
    }
  }

  implicit class IriStringContext(sc: StringContext) {
    def iri(expressions: String*): Iri = macro Macros.contextual[IriInterpolator.type]
  }

}
