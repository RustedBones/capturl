package fr.davit.capturl.scaladsl

import fr.davit.capturl.parsers.FragmentParser
import org.parboiled2.Parser.DeliveryScheme.Throw

sealed trait Fragment {
  def isEmpty: Boolean
  def nonEmpty: Boolean = !isEmpty
}

object Fragment {

  val empty: Fragment = Empty

  def apply(fragment: String): Fragment = {
    FragmentParser(fragment).phrase(_.ifragment)
  }

  final case class Identifier private[capturl] (value: String) extends Fragment {
    override def isEmpty: Boolean = false
    override def toString: String = value
  }

  case object Empty extends Fragment {
    override def isEmpty: Boolean = true
    override def toString: String = ""
  }
}
