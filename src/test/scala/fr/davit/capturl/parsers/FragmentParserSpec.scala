package fr.davit.capturl.parsers

import fr.davit.capturl.parsers.ParserFixture.TestParser
import fr.davit.capturl.scaladsl.{Fragment, Path}
import org.parboiled2.ParserInput
import org.scalatest.{FlatSpec, Matchers}

class FragmentParserSpec extends FlatSpec with Matchers {

  trait Fixture extends ParserFixture[Fragment] {
    override def createParser(input: ParserInput) = new TestParser[Fragment](input) with FragmentParser {
      override def rule = ifragment
    }
  }

  "FragmentParser" should "parse fragment" in new Fixture {
    parse("") shouldBe Fragment.Identifier("")                     -> ""
    parse("identifier") shouldBe Fragment.Identifier("identifier") -> ""

    // relax parsing
    parse("fragment with spaces") shouldBe Fragment.Identifier("fragment with spaces") -> ""
  }

}
