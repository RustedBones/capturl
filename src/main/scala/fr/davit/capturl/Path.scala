package fr.davit.capturl

import fr.davit.capturl.parsers.PathParser
import org.parboiled2.Parser.DeliveryScheme.Throw

abstract class Path

object Path {

  sealed trait PathElement extends Path

  sealed trait PathDelimiter extends Path

  case object End extends PathElement with PathDelimiter

  final case class Slash(next: PathElement) extends PathDelimiter

  final case class Segment private[capturl] (value: String, next: PathDelimiter) extends PathElement

  object Segment {
    def apply(segment: String, next: PathDelimiter): Segment = {
      new Segment(PathParser(segment).phraseSB(_.isegment), next)
    }
  }

  val empty: Path = End

  def apply(path: String): Path = {
    PathParser(path).phrase(_.ipath)
  }
}
