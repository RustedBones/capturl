package fr.davit.capturl.parsers

import fr.davit.capturl.scaladsl._
import org.parboiled2.{CharPredicate, Rule1, Rule2, RuleN}
import shapeless.{Path => _, _}

object IriParser {
  def apply(iri: String): StringParser with IriParser = {
    new StringParser(iri) with IriParser
  }
}

trait IriParser
    extends SchemeParser
    with AuthorityParser
    with PathParser
    with QueryParser
    with FragmentParser { this: StringParser =>

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

  def IRI: Rule1[StrictIri] = rule {
    ((`iabsolute-part` | `irelative-part`) ~ atomic("?" ~ iquery).?.named("query") ~ atomic("#" ~ ifragment).?.named("fragment")) ~> {
      (scheme: Scheme, authority: Authority, path: Path, query: Option[Query], fragment: Option[Fragment]) =>
        StrictIri(scheme, authority, path, query.getOrElse(Query.empty), fragment.getOrElse(Fragment.empty))
    }
  }


  // Raw Iri parts parsers
  // [scheme:][//authority][path][?query][#fragment]
  def rawScheme: Rule1[Option[String]] = rule {
    optional(capture(oneOrMore(!':' ~ ANY)) ~ ':')
  }

  def rawAuthority: Rule1[Option[String]] = rule {
    optional("//" ~ capture(zeroOrMore(!CharPredicate('/', '?', '#') ~ ANY)))
  }

  def rawPath: Rule1[Option[String]] = rule {
    optional(capture(oneOrMore(!CharPredicate('?', '#') ~ ANY)))
  }

  def rawQuery: Rule1[Option[String]] = rule {
    optional('?' ~ capture(zeroOrMore(!'#' ~ ANY)))
  }

  def rawFragment: Rule1[Option[String]] = rule {
    optional('#' ~ capture(zeroOrMore(ANY)))
  }

  def IRILazy: Rule1[LazyIri] = rule {
    (rawScheme ~ rawAuthority ~ rawPath ~ rawQuery ~ rawFragment) ~> {
      (scheme: Option[String], authority: Option[String], path: Option[String], query: Option[String], fragment: Option[String]) =>
        LazyIri(scheme, authority, path, query, fragment)
    }
  }
}
