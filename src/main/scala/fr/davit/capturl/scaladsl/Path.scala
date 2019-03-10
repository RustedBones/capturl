package fr.davit.capturl.scaladsl

import fr.davit.capturl.parsers.PathParser
import org.parboiled2.Parser.DeliveryScheme.Throw

import scala.annotation.tailrec
import scala.collection.{LinearSeq, LinearSeqOptimized, mutable}

trait Path extends LinearSeq[String] with LinearSeqOptimized[String, Path] {
  override def newBuilder: mutable.Builder[String, Path] = Path.newBuilder
  override def toString: String = mkString
}

object Path {

  val empty: Path = End

  def apply(path: String): Path = {
    PathParser(path).phrase(_.ipath)
  }

  def newBuilder: mutable.Builder[String, Path] = new mutable.Builder[String, Path] {
    val b = List.newBuilder[String]
    def +=(elem: String): this.type = { b += elem; this }
    def clear() = b.clear()
    def result() = build(b.result().reverse)

    @tailrec
    def build(segments: List[String], path: Option[PathElement] = None): Path =
      segments match {
        case Nil               => path.getOrElse(Slash(End))
        case "" :: Nil         => path.map(Slash).getOrElse(Slash(End))
        case "." :: tail       => build(tail, path)
        case ".." :: "" :: Nil => build("" :: Nil, path) // special case when '..' is just after root
        case ".." :: _ :: tail => build(tail, path)
        case segment :: tail   => build(tail, Some(Segment(segment, path.map(Slash).getOrElse(End))))
      }
  }

  sealed trait PathElement extends Path

  sealed trait PathDelimiter extends Path

  case object End extends PathElement with PathDelimiter {
    override def isEmpty: Boolean = true
    override def head: String     = throw new NoSuchElementException("head of empty path")
    override def tail: Path       = throw new UnsupportedOperationException("tail of empty path")
  }

  final case class Slash(override val tail: PathElement) extends PathDelimiter {
    override def isEmpty: Boolean = false
    override def head: String     = "/"
  }

  final case class Segment private[capturl] (override val head: String, override val tail: PathDelimiter)
      extends PathElement {
    override def isEmpty: Boolean = false
  }

  object Segment {

    def apply(segment: String, next: PathDelimiter): Segment = {
      new Segment(PathParser(segment).phraseSB(_.isegment), next)
    }
  }
}
