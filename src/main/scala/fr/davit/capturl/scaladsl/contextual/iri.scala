package fr.davit.capturl.scaladsl.contextual

import contextual._
import fr.davit.capturl.scaladsl.contextual.iri.IriInterpolator.SchemeContext
import fr.davit.capturl.scaladsl.{Iri, Scheme}

import scala.util.{Failure, Success, Try}

object iri {

  object IriInterpolator extends Interpolator {

    sealed trait ContextType extends Context
    case object NewContext extends ContextType
    case object AuthorityContext extends ContextType
    case object PathContext extends ContextType
    case object QueryContext extends ContextType
    case object FragmentContext extends ContextType

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
          val (contexts, finaleState) = parts.foldLeft((List.empty[ContextType], NewContext: ContextType)) {
            case ((cs, s), Literal(_, "/")) => (PathContext :: cs, Path)
            case (cs, Literal(_, "/")) =>
          }
          contexts
      }
    }

    def evaluate(interpolation: RuntimeInterpolation): Iri = {
      Iri(interpolation.parts.mkString)
    }
  }

  implicit val embedScheme = IriInterpolator.embed[Scheme](
    Case(SchemeContext, _) {
      case Scheme.Protocol(protocol) => protocol
      case _ =>
    }
  )

  implicit class IriStringContext(sc: StringContext) {
    val iri = Prefix(IriInterpolator, sc)
  }

}
