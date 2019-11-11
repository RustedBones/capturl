package fr.davit.capturl.scaladsl

import fr.davit.capturl.javadsl
import fr.davit.capturl.parsers.PathParser
import fr.davit.capturl.scaladsl.Path.{Empty, Segment, Slash, SlashOrEmpty}

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.util.Try

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

  def /(segment: String): Path = this ++ Slash(Segment.parse(segment).get)

  def ?/(segment: String): Path = if (this.endsWithSlash) this + segment else this / segment

  def relativize(path: Path): Path = {

    @tailrec def parentPath(src: Path, dst: Path = Empty): Path = src match {
      case Empty                   => dst // end destination
      case Segment(_, Empty)       => dst // end destination
      case Segment(_, Slash(tail)) => parentPath(tail, Segment("..", Slash(dst)))
      case Slash(tail)             => parentPath(tail) // this should not happen
    }

    @tailrec def rec(base: Path, rel: Path): Path = (base, rel) match {
      case (Empty, _)                   => rel // match all the base
      case (Segment(_, Empty), _)       => rel // match all the base except trailing segment
      case (b, Empty)                   => parentPath(b) // match all rel
      case (b, Segment(segment, Empty)) => parentPath(b, Segment(segment)) // match all rel except trailing segment
      case (b, r) if b.head == r.head   => rec(b.tail, r.tail)
      case _                            => path // no match, can't relativize
    }

    rec(this, path)
  }

  def resolve(path: Path): Path = {
    if (path.isAbsolute) path
    else if (path.isEmpty) this
    else {
      reverse match {
        case Segment(_, tail) => tail.reverseAndPrependTo(path).normalize()
        case reversed         => reversed.reverseAndPrependTo(path).normalize()
      }
    }
  }

  def normalize(): Path = {
    // remove slash when possible
    def collapseSlash(p: Path): Path = p match {
      case Slash(tail) if tail.nonEmpty => tail
      case _                            => p
    }

    @tailrec def process(input: Path, output: SlashOrEmpty = Empty): Path = input match {
      case Slash(tail)        => process(tail, Slash(output))
      case Segment("", tail)  => process(tail, output)
      case Segment(".", tail) => process(collapseSlash(tail), output)
      case Segment("..", tail) =>
        val parent = output match {
          case Empty                       => Slash(Segment(".."))
          case p @ Slash(Segment("..", _)) => Slash(Segment("..", p))
          case Slash(Segment(_, Empty))    => Empty
          case Slash(Segment(_, p))        => p
          case Slash(_)                    => output
        }
        process(collapseSlash(tail), parent)
      case Segment(string, Slash(tail)) => process(tail, Slash(Segment(string, output)))
      case Segment(string, Empty)       => Segment(string, output).reverse
      case Empty                        => output.reverse
    }

    if (isAbsolute) process(tail, Slash()) else process(this)
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

  val empty: Path   = Empty
  val slash: Path   = Slash()
  def root: Path    = slash
  def / : Path      = slash
  def /(path: Path) = Slash(path)

  def apply(path: String): Path = parse(path).get

  def parse(path: String): Try[Path] = {
    PathParser(path).phrase(_.ipath)
  }

  sealed trait SlashOrEmpty extends Path {
    def startsWithSegment = false
  }

  object Segment {

    def parse(segment: String): Try[Segment] = {
      PathParser(segment).phrase(_.isegment)
    }
  }

  final case class Segment(override val head: String, override val tail: SlashOrEmpty = Empty) extends Path {
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
    override protected def reverseAndPrependTo(prefix: Path): Path = prefix match {
      case soe: SlashOrEmpty => tail.reverseAndPrependTo(Segment(head, soe))
      case s: Segment        => throw new Exception(s"Segment $s can't be appended to another segment")
    }
    override def ::(segment: String): Path = Segment.parse(segment).get.copy(tail = tail)
    override def ++(suffix: Path): Path    = head :: (tail ++ suffix)
    override def segments: List[String]    = head :: tail.segments
  }

  case object Empty extends SlashOrEmpty {
    override type Head = Nothing
    override def head: Head                                        = throw new NoSuchElementException("head of empty path")
    override def tail: Path                                        = throw new UnsupportedOperationException("tail of empty path")
    override def isEmpty: Boolean                                  = true
    override def length: Int                                       = 0
    override def startsWithSlash: Boolean                          = false
    override def startsWith(that: Path): Boolean                   = that.isEmpty
    override protected def reverseAndPrependTo(prefix: Path): Path = prefix
    override def ::(segment: String): Path                         = Segment.parse(segment).get
    override def ++(suffix: Path): Path                            = suffix
    override def segments: List[String]                            = Nil
  }

  final case class Slash(override val tail: Path = Empty) extends SlashOrEmpty {
    override type Head = Char
    override def head: Head                                        = '/'
    override def isEmpty: Boolean                                  = false
    override def length: Int                                       = tail.length + 1
    override def startsWithSlash: Boolean                          = true
    override def startsWith(that: Path): Boolean                   = that.isEmpty || that.startsWithSlash && tail.startsWith(that.tail)
    override protected def reverseAndPrependTo(prefix: Path): Path = tail.reverseAndPrependTo(Slash(prefix))
    override def ::(segment: String): Path                         = Segment.parse(segment).get.copy(tail = this)
    override def ++(suffix: Path): Path                            = Path./(tail ++ suffix)
    override def segments: List[String]                            = tail.segments
  }
}
