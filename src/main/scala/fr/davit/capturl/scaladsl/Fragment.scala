package fr.davit.capturl.scaladsl

import fr.davit.capturl.parsers.FragmentParser
import fr.davit.capturl.scaladsl.OptionalPart.{DefinedPart, EmptyPart}
import org.parboiled2.Parser.DeliveryScheme

import scala.util.{Success, Try}

sealed trait Fragment extends OptionalPart[String]

object Fragment {

  val empty: Fragment = Empty

  def apply(fragment: String): Fragment = parse(fragment).get

  def parse(fragment: String): Try[Fragment] = {
    if (fragment.isEmpty) {
      Success(Fragment.Empty)
    } else {
      FragmentParser(fragment).phrase(_.ifragment, "fragment")(DeliveryScheme.Try)
    }
  }

  case object Empty extends Fragment with EmptyPart
  final case class Identifier(value: String) extends Fragment with DefinedPart[String]
}
