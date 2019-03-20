package fr.davit.capturl.parsers
import fr.davit.capturl.scaladsl.Path
import org.parboiled2._

object PathParser {

  def apply(path: String): Parser with PathParser = {
    new StringParser(path) with PathParser
  }
}

trait PathParser extends RichStringBuilding {
  this: Parser =>

  def build(): Path = {
    val str = sb.toString
    if (str.isEmpty) {
      Path.empty
    } else {
      val b = Path.newBuilder
      b ++= sb.toString.split("/")
      b.result()
    }
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

  def `ipath-noscheme`: Rule1[Path] = rule {
    clearSB() ~ `isegment-nz-nc ` ~ ('/' ~ appendSB() ~ isegment).* ~ push(build())
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
