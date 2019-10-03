package fr.davit.capturl

import fr.davit.capturl.scaladsl.Path
import fr.davit.capturl.scaladsl.Path.{Segment, Slash}
import org.scalatest.{FlatSpec, Matchers}

class PathSpec extends FlatSpec with Matchers {

  "Path" should "tell if it is empty" in {
    Path.empty.isEmpty shouldBe true
    Path.root.isEmpty shouldBe false
    Path("test").isEmpty shouldBe false
    Path("test/").isEmpty shouldBe false
  }

  it should "tell if it starts with slash" in {
    Path.empty.startsWithSlash shouldBe false
    Path.root.startsWithSlash shouldBe true
    Path("test").startsWithSlash shouldBe false
    Path("test/").startsWithSlash shouldBe false
  }

  it should "tell if it starts with segment" in {
    Path.empty.startsWithSegment shouldBe false
    Path.root.startsWithSlash shouldBe true
    Path("test").startsWithSlash shouldBe false
    Path("test/").startsWithSlash shouldBe false
  }

  it should "provide segments" in {
    Path.empty.segments shouldBe Seq.empty[String]
    Path.root.segments shouldBe Seq.empty[String]
    Path("/segment1/segment2/segment3/").segments shouldBe Seq("segment1", "segment2", "segment3")
    Path("segment1/segment2/segment3").segments shouldBe Seq("segment1", "segment2", "segment3")
  }

  it should "normalize paths" in {
    Path("") shouldBe Path.empty
    Path(".") shouldBe Path.empty
    Path("/segment1//segment2") shouldBe Slash(Segment("segment1", Slash(Segment("segment2"))))
    Path("/segment1/./segment2") shouldBe Slash(Segment("segment1", Slash(Segment("segment2"))))
    Path("/segment1/../segment2") shouldBe Slash(Segment("segment2"))
    Path("segment1/../segment2") shouldBe Segment("segment2")
    Path("/../segment2") shouldBe Slash(Segment("segment2"))
    Path("../segment2") shouldBe Segment("..", Slash(Segment("segment2")))
    Path("../../segment2") shouldBe Segment("..", Slash(Segment("..", Slash(Segment("segment2")))))
  }

  it should "resolve paths" in {
    Path.root.resolve(Path.empty) shouldBe Path.root
    Path.root.resolve(Path("/absolute")) shouldBe Slash(Segment("absolute"))
    Path.root.resolve(Path("relative")) shouldBe Slash(Segment("relative"))
    Path("/directory/file").resolve(Path("otherFile")) shouldBe Slash(Segment("directory", Slash(Segment("otherFile"))))
    Path("relative/file").resolve(Path("otherFile")) shouldBe Segment("relative", Slash(Segment("otherFile")))
    Path("/directory1/directory2/").resolve(Path("../otherFile")) shouldBe Slash(Segment("directory1", Slash(Segment("otherFile"))))
    Path.root.resolve(Path("../otherFile")) shouldBe Slash(Segment("otherFile"))
    Path.root.resolve(Path("../../otherFile")) shouldBe Slash(Segment("otherFile"))
  }

  it should "relativize paths" in {
    Path.empty.relativize(Path("/base")) shouldBe Slash(Segment("base"))
    Path.root.relativize(Path("/base")) shouldBe Segment("base")
    Path("/base/").relativize(Path("/base/otherFile")) shouldBe Segment("otherFile")
    Path("/base/file").relativize(Path("/base/otherFile")) shouldBe Segment("otherFile")
    Path("/base/too/deep/").relativize(Path("/base/file")) shouldBe Segment("..", Slash(Segment("..", Slash(Segment("file")))))
    Path("relativeBase/").relativize(Path("relativeBase/otherFile")) shouldBe Segment("otherFile")
    Path("relativeBase/").relativize(Path("otherBase/")) shouldBe Segment("otherBase", Slash())
  }
}
