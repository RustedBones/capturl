package fr.davit.capturl.parsers

import org.parboiled2._

class PathParser extends RichStringBuilding {
  this: Parser =>

  def isegment: Rule0 = rule {
    ipchar.*
  }

  def `isegment-nz`: Rule0 = rule {
    ipchar.+
  }

  def `isegment-nz-nc `: Rule0 = rule {
    (iunreserved | `pct-encoded` | `sub-delims` | '@' ~ appendSB()).+
  }

  def `ipath-abempty`: Rule0 = rule {
    '/' ~ appendSB() ~ isegment
  }

  def `ipath-absolute` = rule {
    '/' ~ appendSB() ~ (`isegment-nz` | `ipath-abempty`.*)
  }

  def `ipath-rootless` = rule {
    `isegment-nz` ~  `ipath-abempty`.*
  }

  def `ipath-empty` = rule {
    &(!ipchar)
  }
}
