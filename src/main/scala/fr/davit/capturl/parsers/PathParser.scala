package fr.davit.capturl.parsers
import fr.davit.capturl.scaladsl.Path
import fr.davit.capturl.scaladsl.Path.{Segment, Slash}
import org.parboiled2._

object PathParser {

  def apply(path: String): Parser with PathParser = {
    new StringParser(path) with PathParser
  }
}

trait PathParser extends RichStringBuilding {
  this: Parser =>

  def isegment: Rule1[Segment] = rule {
    clearSB() ~ ipchar.* ~ push(Segment(sb.toString))
  }

  def `isegment-nz`: Rule1[Segment] = rule {
    clearSB() ~ ipchar.+ ~ push(Segment(sb.toString))
  }

  def `isegment-nz-nc ` : Rule1[Segment] = rule {
    clearSB() ~ (iunreserved | `pct-encoded` | `sub-delims` | '@' ~ appendSB()).+ ~ push(Segment(sb.toString))
  }

  def `ipath-abempty`: Rule1[Path] = rule {
    // construct in reverse for better performance
    push(Path.empty) ~ ('/' ~ isegment ~> ((p: Path, s: Segment) => s ++ Slash(p))).* ~> ((p: Path) => p.reverse)
  }

  def `ipath-absolute`: Rule1[Path] = rule {
    '/' ~ push(Path.root) ~ (`isegment-nz` ~ `ipath-abempty` ~> ((r: Path, s: Segment, p: Path) => r ++ s ++ p)).?
  }

  def `ipath-noscheme`: Rule1[Path] = rule {
    `isegment-nz-nc ` ~ `ipath-abempty` ~> ((s: Segment, p: Path) => s ++ p)
  }

  def `ipath-rootless`: Rule1[Path] = rule {
    `isegment-nz` ~ `ipath-abempty` ~> ((s: Segment, p: Path) => s ++ p)
  }

  def `ipath-empty`: Rule1[Path] = rule {
    clearSB() ~ &(!ipchar) ~ push(Path.empty)
  }

  def ipath: Rule1[Path] = rule {
    (`ipath-rootless` | `ipath-absolute` | `ipath-abempty` | `ipath-empty`) ~> ((path: Path) => path.normalize())
  }
}
