package fr.davit.capturl.parsers

import fr.davit.capturl.scaladsl._
import org.parboiled2.{Parser, Rule1, Rule2, RuleN}
import shapeless.{Path => _, _}

trait IriParser
    extends SchemeParser
    with AuthorityParser
    with PathParser
    with QueryParser
    with FragmentParser { this: Parser =>

  def `ihier -part`: Rule2[Authority, Path] = rule {
    ("//" ~ iauthority ~ `ipath-abempty`) |
      (`ipath-absolute` | `ipath-rootless` | `ipath-empty`) ~> ((path: Path) => Authority.empty :: path :: HNil)
  }

  def `iabsolute-part`: RuleN[Scheme :: Authority :: Path :: HNil] = rule {
    scheme ~ ":" ~ `ihier -part`
  }

  def `irelative-part`: RuleN[Scheme :: Authority :: Path :: HNil] = rule {
    ("//" ~ iauthority ~ `ipath-abempty`) ~> ((authority: Authority, path: Path) => Scheme.empty :: authority :: path :: HNil) |
      (`ipath-absolute` | `ipath-noscheme` | `ipath-empty`) ~> ((path: Path) => Scheme.empty :: Authority.empty :: path :: HNil)
  }

  def IRI: Rule1[Iri] = rule {
    ((`iabsolute-part` | `irelative-part`) ~ ("?" ~ iquery).? ~ ("#" ~ ifragment).?) ~> {
      (scheme: Scheme, authority: Authority, path: Path, query: Option[Query], fragment: Option[Fragment]) =>
        Iri(scheme, authority, path, query.getOrElse(Query.empty), fragment.getOrElse(Fragment.empty))
    }
  }

}
