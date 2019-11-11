package fr.davit.capturl.scaladsl

import fr.davit.capturl.javadsl
import fr.davit.capturl.parsers._

import scala.util.{Failure, Success, Try}

sealed abstract class Iri private[capturl] extends javadsl.Iri {
  def scheme: Scheme
  def rawScheme: Option[String]
  def authority: Authority
  def rawAuthority: Option[String]
  def path: Path
  def rawPath: Option[String]
  def query: Query
  def rawQuery: Option[String]
  def fragment: Fragment
  def rawFragment: Option[String]

  private[capturl] def normalizedAuthority: Authority
  private[capturl] def normalizedPath: Path

  def toStrict: StrictIri

  def isAbsolute: Boolean = scheme.nonEmpty
  def isRelative: Boolean = !isAbsolute

  def withScheme(scheme: Scheme): Iri
  def withAuthority(authority: Authority): Iri
  def withPath(path: Path): Iri
  def withQuery(query: Query): Iri
  def withFragment(fragment: Fragment): Iri

  def relativize(iri: Iri): Iri

  def resolve(iri: Iri): Iri

  /* Java API */
  override def getScheme: String                                        = scheme.toString
  override def getRawScheme: String                                     = rawScheme.getOrElse("")
  override def getAuthority: javadsl.Authority                          = authority
  override def getRawAuthority: String                                  = rawAuthority.getOrElse("")
  override def withAuthority(authority: javadsl.Authority): javadsl.Iri = withAuthority(authority.asScala)
  override def getPath: javadsl.Path                                    = path
  override def getRawPath: String                                       = rawPath.getOrElse("")
  override def withPath(path: javadsl.Path): javadsl.Iri                = withPath(path.asScala)
  override def getQuery: javadsl.Query                                  = query
  override def getRawQuery: String                                      = rawQuery.getOrElse("")
  override def withQuery(query: javadsl.Query): javadsl.Iri             = withQuery(query.asScala)
  override def getFragment: String                                      = fragment.toString
  override def getRawFragment: String                                   = rawFragment.getOrElse("")
  override def relativize(iri: javadsl.Iri): javadsl.Iri                = relativize(iri.asScala)
  override def resolve(iri: javadsl.Iri): javadsl.Iri                   = resolve(iri.asScala)
  override def asScala(): Iri                                           = this

  override def equals(other: Any): Boolean = other match {
    case that: Iri => this.toString == that.toString
    case _         => false
  }

  override def hashCode(): Int = toString.hashCode
}

object Iri {

  /**
    * Normalize string to option of non empty string
    */
  def rawString(value: String): Option[String] = Option(value).filter(_.nonEmpty)

  /**
    * Remove port from authority if this is the deafault protocol port
    */
  def normalizeAuthority(scheme: Scheme, authority: Authority): Authority = {
    if (Scheme.defaultPort(scheme).contains(authority.port)) authority.withPort(Authority.Port.empty) else authority
  }

  /**
    * If Iri is absolute or has an authority defined. Makes path absolute
    */
  def normalizePath(scheme: Scheme, authority: Authority, path: Path): Path = {
    if (scheme.nonEmpty || authority.nonEmpty) Path.root.resolve(path) else path
  }

  def normalizeRawPath(scheme: Option[String], authority: Option[String], path: Option[String]): Option[String] = {
    if (scheme.nonEmpty || authority.nonEmpty) {
      path match {
        case None                         => Some("/")
        case Some(p) if p.startsWith("/") => Some(p)
        case Some(p)                      => Some(s"/$p")
      }
    } else {
      path
    }
  }

  sealed trait ParsingMode

  object ParsingMode {
    case object Strict extends ParsingMode
    case object Lazy extends ParsingMode

    def apply(mode: String): ParsingMode = mode match {
      case "strict" => Strict
      case "lazy"   => Lazy
      case _        => throw new IllegalArgumentException(mode + " is not a legal ParsingMode")
    }
  }

  val empty: Iri = StrictIri()

  def apply(iri: String, parsingMode: ParsingMode = ParsingMode.Strict): Iri = parse(iri, parsingMode).get

  def parse(iri: String, parsingMode: ParsingMode = ParsingMode.Strict): Try[Iri] = {
    parsingMode match {
      case ParsingMode.Strict => IriParser(iri).phrase(_.IRI)
      case ParsingMode.Lazy   => IriParser(iri).phrase(_.IRILazy)
    }
  }
}

final case class StrictIri(
    scheme: Scheme = Scheme.empty,
    authority: Authority = Authority.empty,
    path: Path = Path.empty,
    query: Query = Query.empty,
    fragment: Fragment = Fragment.empty
) extends Iri {

  override def isSchemeValid: Boolean       = true
  override def rawScheme: Option[String]    = scheme.toOption.map(_.toString)
  override def isAuthorityValid: Boolean    = true
  override def rawAuthority: Option[String] = Option(authority).filter(_.nonEmpty).map(_.toString)
  override def isPathValid: Boolean         = true
  override def rawPath: Option[String]      = Option(path).filter(_.nonEmpty).map(_.toString)
  override def isQueryValid: Boolean        = true
  override def rawQuery: Option[String]     = Option(query).filter(_.nonEmpty).map(_.toString)
  override def isFragmentValid: Boolean     = true
  override def rawFragment: Option[String]  = fragment.toOption.map(_.toString)

  import Iri._

  override private[capturl] lazy val normalizedAuthority: Authority = normalizeAuthority(scheme, authority)

  override private[capturl] lazy val normalizedPath: Path = normalizePath(scheme, authority, path)

  override def toStrict: StrictIri = this

  override def isValid: Boolean = true

  override def withScheme(scheme: Scheme): Iri = copy(scheme = scheme)
  override def withScheme(scheme: String): Iri = withScheme(Scheme(scheme))

  override def withAuthority(authority: Authority): Iri = copy(authority = authority)
  override def withAuthority(authority: String): Iri    = withAuthority(Authority(authority))

  override def withPath(path: Path): Iri   = copy(path = path)
  override def withPath(path: String): Iri = withPath(Path(path))

  override def withQuery(query: Query): Iri  = copy(query = query)
  override def withQuery(query: String): Iri = withQuery(Query(query))

  override def withFragment(fragment: Fragment): Iri = copy(fragment = fragment)
  override def withFragment(fragment: String): Iri   = withFragment(Fragment(fragment))

  override def relativize(iri: Iri): Iri = iri match {
    case strictIri @ StrictIri(s, a, p, _, _) =>
      if (s.nonEmpty && s != scheme) iri
      else if (a.nonEmpty && strictIri.normalizedAuthority != normalizedAuthority) iri
      else strictIri.copy(scheme = Scheme.empty, authority = Authority.empty, path = path.relativize(p))
    case lazyIri @ LazyIri(s, a, p, _, _) =>
      if (s.nonEmpty && !s.contains(scheme.toString)) lazyIri
      else if (a.nonEmpty && lazyIri.isAuthorityValid && lazyIri.normalizedAuthority != normalizedAuthority) lazyIri
      else {
        val rawRelPath = lazyIri.pathResult.map(p => path.relativize(p).toString).toOption.orElse(p)
        lazyIri.copy(rawScheme = None, rawAuthority = None, rawPath = rawRelPath)
      }
  }

  override def resolve(iri: Iri): Iri = iri match {
    case strictIri @ StrictIri(s, a, p, _, _) =>
      if (s.nonEmpty) strictIri
      else if (a.nonEmpty) strictIri.copy(scheme = scheme)
      else strictIri.copy(scheme = scheme, authority = authority, path = path.resolve(p))
    case lazyIri @ LazyIri(s, a, p, _, _) =>
      if (s.nonEmpty) lazyIri
      else if (a.nonEmpty) lazyIri.withScheme(scheme)
      else {
        val rawResPath = lazyIri.pathResult.map(p => path.resolve(p).toString).toOption.orElse(p)
        lazyIri.copy(
          rawScheme = rawString(scheme.toString),
          rawAuthority = rawString(authority.toString),
          rawPath = rawResPath)
      }
  }

  override def toString: String = {
    val b = new StringBuilder()
    if (scheme.nonEmpty) b.append(s"$scheme:")
    if (normalizedAuthority.nonEmpty) b.append(s"//$normalizedAuthority")
    if (normalizedPath.nonEmpty) b.append(normalizedPath)
    if (query.nonEmpty) b.append(s"?$query")
    if (fragment.nonEmpty) b.append(s"#$fragment")
    b.toString
  }
}

final case class LazyIri(
    rawScheme: Option[String] = None,
    rawAuthority: Option[String] = None,
    rawPath: Option[String] = None,
    rawQuery: Option[String] = None,
    rawFragment: Option[String] = None
) extends Iri {

  import Iri._

  private[capturl] lazy val schemeResult: Try[Scheme] = rawScheme match {
    case Some(s) => Scheme.parse(s)
    case None    => Success(Scheme.empty)
  }

  private[capturl] lazy val authorityResult: Try[Authority] = rawAuthority match {
    case Some(a) => Authority.parse(a)
    case None    => Success(Authority.empty)
  }

  private[capturl] lazy val pathResult: Try[Path] = rawPath match {
    case Some(p) => Path.parse(p)
    case None    => Success(Path.empty)
  }

  private[capturl] lazy val queryResult: Try[Query] = rawQuery match {
    case Some(q) => Query.parse(q)
    case None    => Success(Query.empty)
  }

  private[capturl] lazy val fragmentResult: Try[Fragment] = rawFragment match {
    case Some(f) => Fragment.parse(f)
    case None    => Success(Fragment.empty)
  }

  private[capturl] lazy val iriResult: Try[StrictIri] = for {
    s <- schemeResult
    a <- authorityResult
    p <- pathResult
    q <- queryResult
    f <- fragmentResult
  } yield StrictIri(s, a, p, q, f)

  private lazy val normalizedAuthorityResult: Try[Authority] = {
    val normalizedAuthorityResult = for {
      s <- schemeResult
      a <- authorityResult
    } yield normalizeAuthority(s, a)
    normalizedAuthorityResult.orElse(authorityResult)
  }

  private[capturl] lazy val normalizedPathResult: Try[Path] = {
    pathResult.map { p =>
      if (rawScheme.nonEmpty || rawAuthority.nonEmpty) Path.root.resolve(p) else p
    }
  }

  @throws[Exception]
  private def unsafe[T](result: Try[T]): T = {
    result match {
      case Success(value) => value
      case Failure(e)     => throw new Exception(e) // throw a new exception here to know where the unsafe access was made
    }
  }

  override def isValid: Boolean          = iriResult.isSuccess
  override def toStrict: StrictIri       = unsafe(iriResult)
  override def isSchemeValid: Boolean    = schemeResult.isSuccess
  override def scheme: Scheme            = unsafe(schemeResult)
  override def isAuthorityValid: Boolean = authorityResult.isSuccess
  override def authority: Authority      = unsafe(authorityResult)
  override def isPathValid: Boolean      = pathResult.isSuccess
  override def path: Path                = unsafe(pathResult)
  override def isQueryValid: Boolean     = queryResult.isSuccess
  override def query: Query              = unsafe(queryResult)
  override def isFragmentValid: Boolean  = fragmentResult.isSuccess
  override def fragment: Fragment        = unsafe(fragmentResult)

  override private[capturl] lazy val normalizedAuthority: Authority = unsafe(normalizedAuthorityResult)
  override private[capturl] lazy val normalizedPath: Path           = unsafe(normalizedPathResult)

  override def withScheme(scheme: Scheme): Iri = withScheme(scheme.toString)
  override def withScheme(scheme: String): Iri = copy(rawScheme = rawString(scheme))

  override def withAuthority(authority: Authority): Iri = withAuthority(authority.toString)
  override def withAuthority(authority: String): Iri    = copy(rawAuthority = rawString(authority))

  override def withPath(path: Path): Iri   = withPath(path.toString)
  override def withPath(path: String): Iri = copy(rawPath = rawString(path))

  override def withQuery(query: Query): Iri  = withQuery(query.toString)
  override def withQuery(query: String): Iri = copy(rawQuery = rawString(query))

  override def withFragment(fragment: Fragment): Iri = withFragment(fragment.toString)
  override def withFragment(fragment: String): Iri   = copy(rawFragment = rawString(fragment))

  override def relativize(iri: Iri): Iri = iri match {
    case strictIri @ StrictIri(s, a, p, _, _) =>
      if (s.nonEmpty && !rawScheme.contains(s.toString)) strictIri
      else if (a.nonEmpty && (!isAuthorityValid || normalizedAuthority != strictIri.normalizedAuthority)) strictIri
      else {
        val relPath = pathResult.map(_.relativize(p)).getOrElse(p)
        strictIri.copy(scheme = Scheme.empty, authority = Authority.empty, path = relPath)
      }
    case lazyIri @ LazyIri(s, a, p, _, _) =>
      if (s.nonEmpty && s != rawScheme) lazyIri
      else if (a.nonEmpty &&
               ((isAuthorityValid && lazyIri.isAuthorityValid && normalizedAuthority != lazyIri.normalizedAuthority) || // valid different
               (isAuthorityValid != lazyIri.isAuthorityValid) || // on valid the other not
               (rawAuthority != a))) { // both invalid
        lazyIri
      } else {
        val rawRelPath = (for {
          basePath   <- pathResult
          targetPath <- lazyIri.pathResult
        } yield basePath.relativize(targetPath).toString).toOption.orElse(p)
        lazyIri.copy(rawScheme = None, rawAuthority = None, rawPath = rawRelPath)
      }
  }

  override def resolve(iri: Iri): Iri = iri match {
    case strictIri @ StrictIri(s, a, p, q, f) =>
      if (s.nonEmpty) {
        strictIri
      } else if (a.nonEmpty) {
        schemeResult
          .map(baseScheme => strictIri.copy(scheme = baseScheme))
          .getOrElse {
            LazyIri(
              rawScheme,
              rawString(a.toString),
              rawString(p.toString),
              rawString(q.toString),
              rawString(f.toString))
          }
      } else {
        val resolvedResult = for {
          baseScheme    <- schemeResult
          baseAuthority <- authorityResult
          basePath      <- pathResult
        } yield strictIri.copy(scheme = baseScheme, baseAuthority, path = basePath.resolve(p))
        resolvedResult.getOrElse {
          LazyIri(rawScheme, rawAuthority, rawString(p.toString), rawString(q.toString), rawString(f.toString))
        }
      }
    case lazyIri @ LazyIri(s, a, p, _, _) =>
      if (s.nonEmpty) lazyIri
      else if (a.nonEmpty) lazyIri.copy(rawScheme = rawScheme)
      else {
        val rawResPath = (for {
          basePath   <- pathResult
          targetPath <- lazyIri.pathResult
        } yield basePath.resolve(targetPath).toString).toOption.orElse(p)
        lazyIri.copy(rawScheme = rawScheme, rawAuthority = rawAuthority, rawPath = rawResPath)
      }
  }

  override def toString: String = {
    val rawNormalizedPath = Iri.normalizeRawPath(rawScheme, rawAuthority, rawPath)
    val b                 = new StringBuilder()
    rawScheme.foreach(s => b.append(s"${schemeResult.getOrElse(s)}:"))
    rawAuthority.foreach(a => b.append(s"//${normalizedAuthorityResult.getOrElse(a)}"))
    rawNormalizedPath.foreach(p => b.append(normalizedPathResult.getOrElse(p)))
    rawQuery.foreach(q => b.append(s"?${queryResult.getOrElse(q)}"))
    rawFragment.foreach(f => b.append(s"#${fragmentResult.getOrElse(f)}"))
    b.toString
  }
}
