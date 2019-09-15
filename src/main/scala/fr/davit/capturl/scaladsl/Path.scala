package fr.davit.capturl.scaladsl

import java.nio.file.{Path => JPath, Paths => JPaths}

import fr.davit.capturl.javadsl
import fr.davit.capturl.parsers.PathParser
import org.parboiled2.Parser.DeliveryScheme.Throw

import scala.collection.JavaConverters._

class Path private (private val jpath: JPath) extends javadsl.Path {

  private[capturl] def this(path: String) = this(JPaths.get(path))

  private lazy val normalizedPath = jpath.normalize()

  override def isEmpty: Boolean = normalizedPath == Path.empty.jpath
  def nonEmpty: Boolean         = !isEmpty

  def isAbsolute: Boolean = jpath.isAbsolute
  def isRelative: Boolean = !isAbsolute

  def relativize(path: Path): Path = new Path(jpath.relativize(path.jpath))

  def resolve(path: Path): Path = {
    if (jpath.endsWith(Path.slash.jpath)) new Path(jpath.resolve(path.jpath))
    else new Path(jpath.resolveSibling(path.jpath))
  }

  def / : Path = /(".")

  def /(segment: String): Path = {
    val a = new Path(jpath.resolve(segment))
    a
  }

  def segments: Seq[String] = jpath.iterator().asScala.map(_.toString).toSeq

  /** Java API */
  override def appendSlash(): javadsl.Path                  = /
  override def appendSegment(segment: String): javadsl.Path = /(segment)
  override def getSegments: java.lang.Iterable[String]      = segments.asJava
  override def relativize(path: javadsl.Path): javadsl.Path = relativize(path.asScala)
  override def resolve(path: javadsl.Path): javadsl.Path    = resolve(path.asScala)
  override def asScala: Path                                = this

  override def toString: String = normalizedPath.toString

  override def equals(o: Any): Boolean = o match {
    case that: Path => that.normalizedPath == this.normalizedPath
    case _          => false
  }

  override def hashCode(): Int = this.normalizedPath.hashCode()
}

object Path {

  val empty: Path              = new Path(JPaths.get(""))
  val slash: Path              = new Path(JPaths.get("/"))
  def root: Path               = slash
  def / : Path                 = root
  def /(segment: String): Path = new Path(JPaths.get(s"/$segment"))

  def apply(path: String): Path = {
    PathParser(path).phrase(_.ipath)
  }
}
