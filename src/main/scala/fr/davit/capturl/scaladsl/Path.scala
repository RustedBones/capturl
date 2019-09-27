package fr.davit.capturl.scaladsl

import java.nio.file.{Path => JPath, Paths => JPaths}

import fr.davit.capturl.javadsl
import fr.davit.capturl.parsers.PathParser
import fr.davit.capturl.scaladsl.Path.{Directory, Empty, Resource, Segment}
import org.parboiled2.Parser.DeliveryScheme.Throw

import scala.collection.JavaConverters._

sealed abstract class Path extends javadsl.Path {

  // underlying java path
  private[capturl] def jpath: JPath

  def nonEmpty: Boolean = !isEmpty

  def isAbsolute: Boolean = jpath.isAbsolute
  def isRelative: Boolean = !isAbsolute

  def relativize(path: Path): Path
  def resolve(path: Path): Path
  override def normalize(): Path

  // this is only exposed for the builders since this does not normalize the path
  private[capturl] def ++(path: Path): Path = path match {
    case Empty        => this
    case f: Resource  => Resource(JPaths.get(toString ++ f.toString))
    case d: Directory => Directory(JPaths.get(toString ++ d.toString))
  }

  def /(segment: String): Path = /(PathParser(segment).phrase(_.`isegment`))

  def /(segment: Segment): Path = segment match {
    case Segment.Empty  => Directory(jpath)
    case Segment.Parent => Directory(jpath.resolve(segment.jpath).normalize())
    case _              => Resource(jpath.resolve(segment.jpath).normalize())
  }

  def / : Path = /(Segment.Empty)

  def segments: Seq[String] = jpath.iterator().asScala.map(_.toString).toSeq

  /** Java API */
  override def appendSlash(): javadsl.Path                  = /
  override def appendSegment(segment: String): javadsl.Path = /(segment)
  override def getSegments: java.lang.Iterable[String]      = segments.asJava
  override def relativize(path: javadsl.Path): javadsl.Path = relativize(path.asScala)
  override def resolve(path: javadsl.Path): javadsl.Path    = resolve(path.asScala)
  override def asScala: Path                                = this

  override def toString: String = jpath.toString
}

object Path {

  private val jEmpty  = JPaths.get("")
  private val jRoot   = JPaths.get("/")
  private val jParent = JPaths.get("..")

  val empty: Path = Empty
  val slash: Path = Directory(jRoot)
  def root: Path  = slash

  object Segment {
    val Empty: Segment  = Segment(jEmpty)
    val Parent: Segment = Segment(jParent)

    def apply(segment: String): Segment = Segment(JPaths.get(segment).normalize())
  }

  final case class Segment(jpath: JPath) extends AnyVal {

    def toPath: Path = jpath match {
      case `jEmpty` => empty
      case _        => Resource(jpath)
    }
  }

  case object Empty extends Path {
    override private[capturl] val jpath: JPath = JPaths.get("")

    override def isEmpty: Boolean = true

    override def isResource: Boolean  = false
    override def isDirectory: Boolean = false

    override def relativize(path: Path): Path = path
    override def resolve(path: Path): Path    = path
    override def normalize(): Path            = this

    override def /(segment: Segment): Path = slash ++ segment.toPath
  }

  object Resource {

    def apply(name: String): Resource = {
      val jpath = JPaths.get(name).normalize()
      require(jpath != Empty.jpath, "Resource must not be empty")
      Resource(jpath)
    }
  }

  final case class Resource(override private[capturl] val jpath: JPath) extends Path {
    override def isEmpty: Boolean = false

    override def isResource: Boolean  = true
    override def isDirectory: Boolean = false

    override def relativize(path: Path): Path = Resource(jpath.relativize(path.jpath))
    override def resolve(path: Path): Path = path match {
      case Empty        => this
      case f: Resource  => Resource(jpath.resolveSibling(f.jpath).normalize())
      case d: Directory => Directory(jpath.resolveSibling(d.jpath).normalize())
    }
    override def normalize(): Path = Resource(jpath.normalize())
  }

  object Directory {

    def apply(name: String): Directory = {
      val jpath = JPaths.get(name).normalize()
      require(jpath != Empty.jpath, "Directory must not be empty")
      Directory(jpath)
    }
  }

  final case class Directory(override private[capturl] val jpath: JPath) extends Path {
    def isRoot: Boolean = jpath == jRoot

    override def isEmpty: Boolean = false

    override def isResource: Boolean  = false
    override def isDirectory: Boolean = true

    override def relativize(path: Path): Path = Directory(jpath.relativize(path.jpath).normalize())
    override def resolve(path: Path): Path = path match {
      case Empty        => this
      case f: Resource  => Resource(jpath.resolve(f.jpath).normalize())
      case d: Directory => Directory(jpath.resolve(d.jpath).normalize())
    }
    override def normalize(): Path = Directory(jpath.normalize())

    // java normalization collapses the trailing slash on directories
    override def toString: String = if (isRoot) "/" else jpath.toString + "/"
  }

  def apply(path: String): Path = {
    PathParser(path).phrase(_.ipath).normalize()
  }
}
