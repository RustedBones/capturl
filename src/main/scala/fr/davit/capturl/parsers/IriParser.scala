package fr.davit.capturl.parsers

import fr.davit.capturl._
import org.parboiled2.{Parser, Rule1}

trait IriParser
    extends SchemeParser
    with AuthorityParser
    with PathParser
    with QueryParser
    with FragmentParser { this: Parser =>

  def `ihier -part`: Rule1[(Authority, Path)] = rule {
    ("//" ~ iauthority ~ `ipath-abempty`) ~> ((authority: Authority, path: Path) => (authority, path)) |
      (`ipath-absolute` | `ipath-rootless` | `ipath-empty`) ~> ((path: Path) => (Authority.empty, path))
  }

  def `iabsolute-part`: Rule1[(Scheme, Authority, Path)] = rule {
    (scheme ~ ":" ~ `ihier -part`) ~> { (scheme: Scheme, authorityAndPath: (Authority, Path)) =>
      val (authority, path) = authorityAndPath
      (scheme, authority, path)
    }
  }

  def `irelative-part`: Rule1[(Scheme, Authority, Path)] = rule {
    ("//" ~ iauthority ~ `ipath-abempty`) ~> ((authority: Authority, path: Path) => (Scheme.empty, authority, path)) |
      `ipath-absolute` ~> ((path: Path) => (Scheme.empty, Authority.empty, path))
  }

  def IRI: Rule1[Iri] = rule {
    ((`iabsolute-part` | `irelative-part`) ~ ("?" ~ iquery).? ~ ("#" ~ ifragment).?) ~> {
      (schemeAndAuthorityAndPath: (Scheme, Authority, Path), query: Option[Query], fragment: Option[Fragment]) =>
        val (scheme, authority, path) = schemeAndAuthorityAndPath
        Iri(scheme, authority, path, query.getOrElse(Query.empty), fragment.getOrElse(Fragment.empty))
    }
  }

}
