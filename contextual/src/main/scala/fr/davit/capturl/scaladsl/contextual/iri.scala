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

import contextual.{Interpolator, Prefix}
import fr.davit.capturl.scaladsl.Iri

import scala.util.{Failure, Success, Try}

object iri {

  object IriInterpolator extends Interpolator {

    override type Input = String

    override type Output = Iri

    override def contextualize(interpolation: StaticInterpolation): Seq[ContextType] = {
      interpolation.parts match {
        case (lit: Literal) :: Nil =>
          Try(Iri(lit.string)) match {
            case Success(_) => Nil
            case Failure(_) => interpolation.abort(lit, 0, s"Invalid IRI ${lit.string}")
          }
        case parts =>
          val hole = parts.collectFirst { case h: Hole => h }.get
          interpolation.abort(hole, "iri: substitutions are not  yet supported")
      }
    }

    def evaluate(interpolation: RuntimeInterpolation): Iri = {
      Iri(interpolation.parts.mkString)
    }
  }

  implicit class IriStringContext(sc: StringContext) {
    val iri = Prefix(IriInterpolator, sc)
  }

}
