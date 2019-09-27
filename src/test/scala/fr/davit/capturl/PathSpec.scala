package fr.davit.capturl

import fr.davit.capturl.scaladsl.Path
import org.scalatest.{FlatSpec, Matchers}

class PathSpec extends FlatSpec with Matchers {

  "Path" should "tell if it is empty" in {
    Path.empty.isEmpty shouldBe true
    Path.root.isEmpty shouldBe false
    Path("test").isEmpty shouldBe false
    Path("test/").isEmpty shouldBe false
  }

  it should "tell if it is absolute" in {
    Path.empty.isAbsolute shouldBe false
    Path.root.isAbsolute shouldBe true
    Path("test").isAbsolute shouldBe false
    Path("test/").isAbsolute shouldBe false
  }

  it should "tell if it is a resource" in {
    Path.empty.isResource shouldBe false
    Path.root.isResource shouldBe false
    Path("test").isResource shouldBe true
    Path("test/").isResource shouldBe false
  }

  it should "tell if it is a directory" in {
    Path.empty.isDirectory shouldBe false
    Path.root.isDirectory shouldBe true
    Path("test").isDirectory shouldBe false
    Path("test/").isDirectory shouldBe true
  }

  it should "provide segments" in {
    Path.empty.segments shouldBe Seq("")
    Path.root.segments shouldBe Seq.empty[String]
    Path("/segment1/segment2/segment3/").segments shouldBe Seq("segment1", "segment2", "segment3")
    Path("segment1/segment2/segment3").segments shouldBe Seq("segment1", "segment2", "segment3")
  }

  it should "normalize paths" in {
    val res = Path("/segment1/../segment2")

    Path(".") shouldBe Path.empty
    Path("/segment1//segment2") shouldBe Path.Resource("/segment1/segment2")
    Path("/segment1/./segment2") shouldBe Path.Resource("/segment1/segment2")
    Path("/segment1/../segment2") shouldBe Path.Resource("/segment2")
  }

  it should "resolve paths" in {
    Path.root.resolve(Path.empty) shouldBe Path.root
    Path.root.resolve(Path("/absolute")) shouldBe Path.Resource("/absolute")
    Path.root.resolve(Path("relative")) shouldBe Path.Resource("/relative")
    Path("/directory/file").resolve(Path("otherFile")) shouldBe Path.Resource("/directory/otherFile")
    Path("relative/file").resolve(Path("otherFile")) shouldBe Path.Resource("relative/otherFile")
    Path("/directory1/directory2/").resolve(Path("../otherFile")) shouldBe Path.Resource("/directory1/otherFile")
  }
}
