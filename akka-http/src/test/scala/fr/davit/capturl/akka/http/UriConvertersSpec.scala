package fr.davit.capturl.akka.http
import akka.http.scaladsl.model.Uri
import fr.davit.capturl.scaladsl.Authority.{Port, UserInfo}
import fr.davit.capturl.scaladsl._
import org.scalatest.{FlatSpec, Matchers}

class UriConvertersSpec extends FlatSpec with Matchers {

  import UriConverters._

  "UriConverters" should "convert a capturl scheme to string" in {
    (Scheme.Empty: String) shouldBe ""
    (Scheme.Protocol("http"): String) shouldBe "http"
  }

  it should "convert a capturl host to Uri.Host" in {
    (Host.Empty: Uri.Host) shouldBe Uri.Host.Empty
    (Host.IPv4Host(List.fill[Byte](4)(0)): Uri.Host) shouldBe Uri.IPv4Host(Array.fill[Byte](4)(0))
    (Host.IPv6Host(List.fill[Byte](16)(0)): Uri.Host) shouldBe Uri.IPv6Host(Array.fill[Byte](16)(0))
    (Host.NamedHost("example.com"): Uri.Host) shouldBe Uri.NamedHost("example.com")
  }

  it should "convert a capturl user info to String" in {
    (UserInfo.Empty: String) shouldBe ""
    ( UserInfo.Credentials("user:password"): String) shouldBe "user:password"
  }

  it should "convert a capturl port to Int" in {
    (Port.Empty: Int) shouldBe 0
    (Port.Number(8080): Int) shouldBe 8080
  }

  it should "convert a capturl authority to Uri.Authority" in {
    (Authority.empty: Uri.Authority) shouldBe Uri.Authority.Empty
    (Authority(
      Host.NamedHost("example.com"),
      Port.Number(8080),
       UserInfo.Credentials("user:password")
    ): Uri.Authority) shouldBe Uri.Authority(Uri.NamedHost("example.com"), 8080, "user:password")
  }

  it should "convert a capturl path to Uri.Path" in {
    (Path.empty: Uri.Path) shouldBe Uri.Path.Empty
    (Path("segment")./ : Uri.Path) shouldBe Uri.Path.Segment("segment", Uri.Path.Slash(Uri.Path.Empty))
  }

  it should "convert a capturl query to Uri.Query" in {
    (Query.Empty: Uri.Query) shouldBe Uri.Query.Empty
    (Query.Part("key", Some("value"), Query.Empty): Uri.Query) shouldBe Uri.Query.Cons("key", "value", Uri.Query.Empty)
  }

  it should "convert a capturl query to Option[String]" in {
    (Query.Empty: Option[String]) shouldBe empty
    (Query.Part("key", None, Query.Empty): Option[String]) shouldBe Some("key")
    (Query.Part("key", Some("value"), Query.Empty): Option[String]) shouldBe Some("key=value")
  }

}
