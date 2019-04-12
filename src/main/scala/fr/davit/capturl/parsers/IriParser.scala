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

  def `ihier-part`: Rule2[Authority, Path] = rule {
    ("//" ~ iauthority ~ atomic(`ipath-abempty`).named("absolute or empty path")) |
      (atomic(`ipath-absolute`).named("absolute path") |
        atomic(`ipath-rootless`).named("rootless path") |
        atomic(`ipath-empty`).named("empty path")) ~> {
        path: Path => Authority.empty :: path :: HNil
      }
  }

  def `iabsolute-part`: RuleN[Scheme :: Authority :: Path :: HNil] = rule {
    atomic(scheme) ~ ":" ~ `ihier-part`
  }

  def `irelative-part`: RuleN[Scheme :: Authority :: Path :: HNil] = rule {
    ("//" ~ iauthority ~ atomic(`ipath-abempty`).named("absolute or empty path")) ~> ((authority: Authority, path: Path) => Scheme.empty :: authority :: path :: HNil) |
      (atomic(`ipath-absolute`).named("absolute path") |
        atomic(`ipath-noscheme`).named("no scheme path") |
        atomic(`ipath-empty`).named("empty path")) ~> ((path: Path) => Scheme.empty :: Authority.empty :: path :: HNil)
  }

  def IRI: Rule1[Iri] = rule {
    ((`iabsolute-part` | `irelative-part`) ~ atomic("?" ~ iquery).?.named("query") ~ atomic("#" ~ ifragment).?.named("fragment") ~ EOI) ~> {
      (scheme: Scheme, authority: Authority, path: Path, query: Option[Query], fragment: Option[Fragment]) =>
        Iri(scheme, authority, path, query.getOrElse(Query.empty), fragment.getOrElse(Fragment.empty))
    }
  }

}
