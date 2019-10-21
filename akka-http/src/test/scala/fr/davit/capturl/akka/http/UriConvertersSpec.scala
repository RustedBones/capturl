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
      val akkaQuery = Uri.Query.Cons("key", "value", Uri.Query.Empty)
      val query = Query.Part("key", Some("value"))
      (akkaQuery: Query) shouldBe query
      (query: Uri.Query) shouldBe akkaQuery
      (query: Option[String]) shouldBe Some("key=value")
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
      val iri = Iri("http://example.com/directory/file.html?key=value#identifier")
      (uri: Iri) shouldBe iri
      (iri: Uri) shouldBe uri
    }

    {
      // unicode iri
      val uri = Uri("http://xn--d1abbgf6aiiy.xn--p1ai/%D0%B4%D0%BE%D0%BA%D1%83%D0%BC%D0%B5%D0%BD%D1%82%D1%8B")
      val iri = Iri("http://президент.рф/документы")
      (uri: Iri) shouldBe iri
      (iri: Uri) shouldBe uri
    }

    {
      // lazy iri (broken query)
      val uri = Uri("http://example.com/?wrong%encoding")
      val iri = Iri("http://example.com/?wrong%encoding", Iri.ParsingMode.Lazy)
      (uri: Iri) shouldBe iri
      (iri: Uri) shouldBe uri
    }

    {
      // path containing double slashes
      val uri = Uri("https://cdn.shotpixel.ai/client/q_glossy,ret_img,w_419/https://releastyuk.co.uk/wp-content/uploads/2019/08/home_imprments.jpeg")
      val iri = Iri("https://cdn.shotpixel.ai/client/q_glossy,ret_img,w_419/https://releastyuk.co.uk/wp-content/uploads/2019/08/home_imprments.jpeg")
      (uri: Iri) shouldBe iri
      (iri: Uri) shouldBe uri
    }
  }
}
