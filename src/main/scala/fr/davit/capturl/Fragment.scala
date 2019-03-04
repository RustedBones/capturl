package fr.davit.capturl

import fr.davit.capturl.parsers.FragmentParser
import org.parboiled2.Parser.DeliveryScheme.Throw

final case class Fragment private[capturl] (value: String)

object Fragment {

  val empty: Fragment = new Fragment("")

  def apply(fragment: String): Fragment = {
    FragmentParser(fragment).phrase(_.ifragment)
  }
}
