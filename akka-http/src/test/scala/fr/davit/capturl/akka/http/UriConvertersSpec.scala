package fr.davit.capturl.akka.http
import akka.http.scaladsl.model.Uri
import fr.davit.capturl.scaladsl.Authority.{Port, UserInfo}
import fr.davit.capturl.scaladsl.Path.{Segment, Slash}
import fr.davit.capturl.scaladsl._
import org.scalatest.{FlatSpec, Matchers}

class UriConvertersSpec extends FlatSpec with Matchers {

  import UriConverters._

  "UriConverters" should "convert schemes" in {
    {
      val akkaScheme = ""
      val scheme = Scheme.empty
      (akkaScheme: Scheme) shouldBe scheme
      (scheme: String) shouldBe akkaScheme
    }

    {
      val akkaScheme = "http"
      val scheme = Scheme.HTTP
      (akkaScheme: Scheme) shouldBe scheme
      (scheme: String) shouldBe akkaScheme
    }
  }

  it should "convert hosts" in {
    {
      val akkaHost = Uri.Host.Empty
      val host = Host.empty
      (akkaHost: Host) shouldBe host
      (host: Uri.Host) shouldBe akkaHost
    }

    {
      val ipv4 = Array.fill[Byte](4)(0)
      val akkaHost = Uri.IPv4Host(ipv4)
      val host = Host.IPv4Host(ipv4)
      (akkaHost: Host) shouldBe host
      (host: Uri.Host) shouldBe akkaHost
    }

    {
      val ipv6 = Array.fill[Byte](16)(0)
      val akkaHost = Uri.IPv6Host(ipv6)
      val host = Host.IPv6Host(ipv6)
      (akkaHost: Host) shouldBe host
      (host: Uri.Host) shouldBe akkaHost
    }

    {
      val domain = "example.com"
      val akkaHost = Uri.NamedHost(domain)
      val host = Host.NamedHost("example.com")
      (akkaHost: Host) shouldBe host
      (host: Uri.Host) shouldBe akkaHost
    }

    {
      val akkaHost = Uri.NamedHost("xn--d1abbgf6aiiy.xn--p1ai")
      val host = Host.NamedHost("президент.рф")
      (akkaHost: Host) shouldBe host
      (host: Uri.Host) shouldBe akkaHost
    }
  }

  it should "convert user info" in {
    {
      val akkaUserInfo = ""
      val userInfo = UserInfo.empty
      (akkaUserInfo: UserInfo) shouldBe userInfo
      (userInfo: String) shouldBe akkaUserInfo
    }

    {
      val akkaUserInfo = "user:password"
      val userInfo = UserInfo.Credentials("user:password")
      (akkaUserInfo: UserInfo) shouldBe userInfo
      (userInfo: String) shouldBe akkaUserInfo
    }
  }

  it should "convert port" in {
    {
      val akkaPort = 0
      val port = Port.empty
      (akkaPort: Port) shouldBe port
      (port: Int) shouldBe akkaPort
    }

    {
      val akkaPort = 8080
      val port = Port.Number(8080)
      (akkaPort: Port) shouldBe port
      (port: Int) shouldBe akkaPort
    }
  }

  it should "convert authority" in {
    {
      val akkaAuthority = Uri.Authority.Empty
      val authority = Authority.empty
      (akkaAuthority: Authority) shouldBe authority
      (authority: Uri.Authority) shouldBe akkaAuthority
    }

    {
      val akkaAuthority = Uri.Authority(Uri.NamedHost("example.com"), 8080, "user:password")
      val authority = Authority(Host.NamedHost("example.com"), Port.Number(8080), UserInfo.Credentials("user:password"))
      (akkaAuthority: Authority) shouldBe authority
      (authority: Uri.Authority) shouldBe akkaAuthority
    }
  }

  it should "convert path" in {
    {
      val akkaPath = Uri.Path.Empty
      val path = Path.empty
      (akkaPath: Path) shouldBe path
      (path: Uri.Path) shouldBe akkaPath
    }

    {
      val akkaPath = Uri.Path.Segment("directory", Uri.Path.Slash(Uri.Path.Segment("file.html", Uri.Path.Empty)))
      val path = Segment("directory", Slash(Segment("file.html")))
      (akkaPath: Path) shouldBe path
      (path: Uri.Path) shouldBe akkaPath
    }

    {
      // non normalized double slash
      val akkaPath = Uri.Path.Segment("directory", Uri.Path.Slash(Uri.Path.Slash(Uri.Path.Segment("file.html", Uri.Path.Empty))))
      val path = Segment("directory", Slash(Segment("", Slash(Segment("file.html")))))
      (akkaPath: Path) shouldBe path.normalize()
      (path: Uri.Path) shouldBe akkaPath
    }
  }

  it should "convert query" in {
    {
      val akkaQuery = Uri.Query.Empty
      val query = Query.empty
      (akkaQuery: Query) shouldBe query
      (query: Uri.Query) shouldBe akkaQuery
      (query: Option[String]) shouldBe empty
    }

    {
      val akkaQuery = Uri.Query.Cons("key", Uri.Query.EmptyValue, Uri.Query.Empty)
      val query = Query.Part("key")
      (akkaQuery: Query) shouldBe query
      (query: Uri.Query) shouldBe akkaQuery
      (query: Option[String]) shouldBe Some("key")
    }

    {
      val akkaQuery = Uri.Query.Cons("key", "value", Uri.Query.Empty)
      val query = Query.Part("key", Some("value"))
      (akkaQuery: Query) shouldBe query
      (query: Uri.Query) shouldBe akkaQuery
      (query: Option[String]) shouldBe Some("key=value")
    }

    {
      val akkaQuery = Uri.Query.Cons("receipt", "café de paris", Uri.Query.Empty)
      val query = Query.Part("receipt", Some("café de paris"))
      (akkaQuery: Query) shouldBe query
      (query: Uri.Query) shouldBe akkaQuery
      (query: Option[String]) shouldBe Some("receipt=caf%C3%A9+de+paris")
    }
  }

  it should "convert fragment" in {
    {
      val akkaFragment = None
      val fragment = Fragment.empty
      (akkaFragment: Fragment) shouldBe fragment
      (fragment: Option[String]) shouldBe akkaFragment
    }

    {
      val akkaFragment = Some("identifier")
      val fragment = Fragment.Identifier("identifier")
      (akkaFragment: Fragment) shouldBe fragment
      (fragment: Option[String]) shouldBe akkaFragment
    }
  }

  it should "convert Uri / Iri" in {
    {
      val uri = Uri()
      val iri = Iri.empty
      (uri: Iri) shouldBe iri
      (iri: Uri) shouldBe uri
    }

    {
      val uri = Uri("http://example.com/directory/file.html?key=value#identifier")
      val iri = Iri("http://example.com/directory/file.html?key=value#identifier", Iri.ParsingMode.Strict)
      val lazyUri = Iri("http://example.com/directory/file.html?key=value#identifier", Iri.ParsingMode.Lazy)
      (uri: Iri) shouldBe iri
      (iri: Uri) shouldBe uri
      (lazyUri: Uri) shouldBe uri
    }

    {
      // unicode iri
      val uri = Uri("http://xn--d1abbgf6aiiy.xn--p1ai" +
        "/%D0%B4%D0%BE%D0%BA%D1%83%D0%BC%D0%B5%D0%BD%D1%82%D1%8B" +
        "?%D1%84%D0%B8%D0%BB%D1%8C%D1%82%D1%80" +
        "#%D0%B7%D0%B0%D0%B3%D0%BB%D0%B0%D0%B2%D0%B8%D0%B5"
      )
      val iri = Iri("http://президент.рф/документы?фильтр#заглавие", Iri.ParsingMode.Strict)
      val lazyUri = Iri("http://президент.рф/документы?фильтр#заглавие", Iri.ParsingMode.Lazy)
      (uri: Iri) shouldBe iri
      (iri: Uri) shouldBe uri
      (lazyUri: Uri) shouldBe uri
    }

    {
      // lazy iri (broken query)
      val uri = Uri("http://example.com/?wrong%encoding")
      val iri = Iri("http://example.com/?wrong%encoding", Iri.ParsingMode.Lazy)
      (uri: Iri) shouldBe iri
      (iri: Uri) shouldBe uri
    }
  }
}
