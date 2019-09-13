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
          val hole = parts.collectFirst {case h: Hole => h }.get
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
