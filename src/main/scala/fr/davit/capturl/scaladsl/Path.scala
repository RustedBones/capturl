package fr.davit.capturl.scaladsl

import fr.davit.capturl.javadsl
import fr.davit.capturl.parsers.PathParser
import fr.davit.capturl.scaladsl.Path.{Empty, Segment, Slash}
import org.parboiled2.Parser.DeliveryScheme.Throw

import scala.annotation.tailrec
import scala.collection.JavaConverters._

sealed abstract class Path extends javadsl.Path {

  type Head

  def head: Head
  def tail: Path

  def nonEmpty: Boolean = !isEmpty

  override def isAbsolute: Boolean = startsWithSlash
  override def isRelative: Boolean = !isAbsolute

  def startsWith(that: Path): Boolean

  def endsWithSlash: Boolean = {
    @tailrec def rec(path: Path): Boolean = path match {
      case Empty             => false
      case Slash(Path.Empty) => true
      case Slash(tail)       => rec(tail)
      case Segment(_, tail)  => rec(tail)
    }
    rec(this)
  }

  final def endsWith(suffix: String, ignoreTrailingSlash: Boolean = false): Boolean = {
    @tailrec def rec(path: Path, lastSegment: String = ""): Boolean =
      path match {
        case Empty               => lastSegment.endsWith(suffix)
        case Slash(Path.Empty)   => ignoreTrailingSlash && lastSegment.endsWith(suffix)
        case Slash(tail)         => rec(tail)
        case Segment(head, tail) => rec(tail, head)
      }
    rec(this)
  }

  def ::(segment: String): Path

  def +(pathString: String): Path = this ++ Path(pathString)

  def ++(suffix: Path): Path

  def reverse: Path = reverseAndPrependTo(Path.Empty)

  protected def reverseAndPrependTo(prefix: Path): Path

  def / : Path = this ++ Slash(Path.Empty)

  def /(segment: String): Path = this ++ Slash(PathParser(segment).phrase(_.isegment))

  def ?/(segment: String): Path = if (this.endsWithSlash) this + segment else this / segment

  def relativize(path: Path): Path = ???

  def resolve(path: Path): Path = {
    if (path.isAbsolute) {
      path
    } else {
      def resolveParent(reversedBase: Path) = (reversedBase, path) match {
        case (Slash(Empty), Segment("..", Empty))                         => Slash() // parent against root
        case (Slash(Empty), Segment("..", Slash(tail)))                   => Slash(tail) // parent against root
        case (Slash(Segment(_, Empty)), Segment("..", Slash(tail)))       => tail // parent against relative folder
        case (Slash(Segment(_, Slash(base))), Segment("..", Slash(tail))) => base.reverseAndPrependTo(Slash(tail))
        case (Slash(Segment(_, base)), Segment("..", Empty))              => base.reverseAndPrependTo(Slash(tail))
        case _                                                            => reversedBase.reverseAndPrependTo(path)
      }

      reverse match {
        case Segment(_, tail) => resolveParent(tail) // drop last segment
        case reversed         => resolveParent(reversed)
      }
    }
  }

  def normalize(): Path = {
    @tailrec def rec(p: Path, normalized: Path = Empty): Path = p match {
      case Empty                                         => normalized
      case Segment("..", Slash(Empty))                   => if (normalized.isEmpty) Slash() else normalized
      case Slash(Segment("", Slash(tail)))               => rec(tail, Slash(normalized)) // collapse double slash
      case Slash(Segment("..", Slash(Segment(_, tail)))) => rec(tail, normalized) // navigate to parent
      case Segment(".", Slash(tail))                     => rec(tail, normalized) // remove current segment
      case Segment("" | ".", tail)                       => rec(tail, normalized) // remove current segment
      case Slash(tail)                                   => rec(tail, Slash(normalized))
      case Segment(segment, tail)                        => rec(tail, Segment(segment, normalized))
    }
    rec(reverse)
  }

  def segments: List[String]

  /** Java API */
  override def appendSlash(): javadsl.Path                  = /
  override def appendSegment(segment: String): javadsl.Path = /(segment)
  override def getSegments: java.lang.Iterable[String]      = segments.asJava
  override def relativize(path: javadsl.Path): javadsl.Path = relativize(path.asScala)
  override def resolve(path: javadsl.Path): javadsl.Path    = resolve(path.asScala)
  override def asScala: Path                                = this

  override def toString: String = {
    val sb      = new StringBuilder()
    var p: Path = this
    while (p.nonEmpty) {
      sb.append(p.head.toString)
      p = p.tail
    }
    sb.result()
  }
}

object Path {

  val empty: Path = Empty
  val slash: Path = Slash()
  def root: Path  = slash

  case object Empty extends Path {
    override type Head = Nothing
    override def head: Head                                        = throw new NoSuchElementException("head of empty path")
    override def tail: Path                                        = throw new UnsupportedOperationException("tail of empty path")
    override def isEmpty: Boolean                                  = true
    override def length: Int                                       = 0
    override def startsWithSlash: Boolean                          = false
    override def startsWithSegment: Boolean                        = false
    override def startsWith(that: Path): Boolean                   = that.isEmpty
    override protected def reverseAndPrependTo(prefix: Path): Path = prefix
    override def ::(segment: String): Path                         = PathParser(segment).phrase(_.isegment)
    override def ++(suffix: Path): Path                            = suffix
    override def segments: List[String]                            = Nil
  }

  final case class Slash(override val tail: Path = Empty) extends Path {
    override type Head = Char
    override def head: Head                                        = '/'
    override def isEmpty: Boolean                                  = false
    override def length: Int                                       = tail.length + 1
    override def startsWithSlash: Boolean                          = true
    override def startsWithSegment: Boolean                        = false
    override def startsWith(that: Path): Boolean                   = that.isEmpty || that.startsWithSlash && tail.startsWith(that.tail)
    override protected def reverseAndPrependTo(prefix: Path): Path = tail.reverseAndPrependTo(Slash(prefix))
    override def ::(segment: String): Path                         = PathParser(segment).phrase(_.isegment).copy(tail = this)
    override def ++(suffix: Path): Path                            = Slash(tail ++ suffix)
    override def segments: List[String]                            = tail.segments
  }

  final case class Segment(override val head: String, override val tail: Path = Empty) extends Path {
    override type Head = String
    override def isEmpty: Boolean           = false
    override def length: Int                = tail.length + 1
    override def startsWithSlash: Boolean   = false
    override def startsWithSegment: Boolean = true
    override def startsWith(that: Path): Boolean = that match {
      case Segment(`head`, t) => tail.startsWith(t)
      case Segment(h, Empty)  => head.startsWith(h)
      case x                  => x.isEmpty
    }
    override protected def reverseAndPrependTo(prefix: Path): Path = tail.reverseAndPrependTo(head :: prefix)
    override def ::(segment: String): Path                         = PathParser(segment + head).phrase(_.isegment).copy(tail = tail)
    override def ++(suffix: Path): Path                            = head :: (tail ++ suffix)
    override def segments: List[String]                            = head :: tail.segments
  }

  def apply(path: String): Path = {
    PathParser(path).phrase(_.ipath).normalize()
  }
}
