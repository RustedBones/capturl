package fr.davit.capturl.scaladsl

import fr.davit.capturl.parsers.PathParser
import fr.davit.capturl.javadsl
import fr.davit.capturl.scaladsl.Path.{End, Segment, Slash}
import org.parboiled2.Parser.DeliveryScheme.Throw

import scala.annotation.tailrec
import scala.collection.{mutable, LinearSeq, LinearSeqOptimized}

import scala.collection.JavaConverters._

sealed trait Path extends javadsl.Path with LinearSeq[String] with LinearSeqOptimized[String, Path] {
  def isAbsolute: Boolean = startsWithSlash
  def isRelative: Boolean = !isAbsolute

  def startsWithSlash: Boolean = this match {
    case Slash(_) => true
    case _        => false
  }
  def startsWithSegment: Boolean = !startsWithSlash && nonEmpty

  def endsWithSlash: Boolean = {
    @tailrec def check(path: Path): Boolean = path match {
      case End ⇒ false
      case Slash(End) ⇒ true
      case Slash(tail) ⇒ check(tail)
      case Segment(_, tail) ⇒ check(tail)
    }
    check(this)
  }

  def segments: Seq[String] = filter(_ != "/")

  /** Java API */
  override def getSegments: java.lang.Iterable[String] = segments.asJava

  override def newBuilder: mutable.Builder[String, Path] = Path.newBuilder
  override def toString: String                          = mkString
}

object Path {

  val empty: Path = End
  val root: Path  = Slash(End)

  def apply(path: String): Path = {
    PathParser(path).phrase(_.ipath)
  }

  def newBuilder: mutable.Builder[String, Path] = new mutable.Builder[String, Path] {
    val b                           = List.newBuilder[String]
    def +=(elem: String): this.type = { b += elem; this }
    def clear()                     = b.clear()
    def result()                    = build(b.result().reverse)

    @tailrec
    def build(segments: List[String], path: Option[PathElement] = None): Path =
      segments match {
        case Nil               => path.getOrElse(Slash(End)) // in the empty case return root ("/".split("/"): Array())
        case "" :: Nil         => path.map(Slash).getOrElse(End) // in the empty case return end ("".split("/"): Array(""))
        case "" :: tail        => build(tail, path) // collapse double slash
        case "." :: tail       => build(tail, path) // collapse current folder
        case ".." :: "" :: Nil => build("" :: Nil, path) // special case when '..' is just after root
        case ".." :: _ :: tail => build(tail, path) // collapse parent folder
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
}
