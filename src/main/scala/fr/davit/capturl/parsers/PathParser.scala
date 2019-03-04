package fr.davit.capturl.parsers

import fr.davit.capturl.Path
import fr.davit.capturl.Path._
import org.parboiled2._

import scala.annotation.tailrec

object PathParser {

  def apply(path: String): Parser with PathParser = {
    new StringParser(path) with PathParser
  }
}

trait PathParser extends RichStringBuilding {
  this: Parser =>

  def build(): Path = {
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

    build(sb.toString.split('/').toList.reverse)
  }

  def isegment: Rule0 = rule {
    ipchar.*
  }

  def `isegment-nz`: Rule0 = rule {
    ipchar.+
  }

  def `isegment-nz-nc ` : Rule0 = rule {
    (iunreserved | `pct-encoded` | `sub-delims` | '@' ~ appendSB()).+
  }

  def `ipath-abempty`: Rule1[Path] = rule {
    clearSB() ~ ('/' ~ appendSB() ~ isegment).* ~ push(build())
  }

  def `ipath-absolute`: Rule1[Path] = rule {
    clearSB() ~ '/' ~ appendSB() ~ (`isegment-nz` ~ ('/' ~ appendSB() ~ isegment).*).? ~ push(build())
  }

  def `ipath-rootless`: Rule1[Path] = rule {
    clearSB() ~ `isegment-nz` ~ ('/' ~ appendSB() ~ isegment).* ~ push(build())
  }

  def `ipath-empty`: Rule1[Path] = rule {
    clearSB() ~ &(!ipchar) ~ push(build())
  }

  def ipath: Rule1[Path] = rule {
    `ipath-rootless` | `ipath-absolute` | `ipath-abempty` | `ipath-empty`
  }
}
