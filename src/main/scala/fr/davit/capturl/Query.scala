package fr.davit.capturl

import fr.davit.capturl.parsers.QueryParser
import org.parboiled2.Parser.DeliveryScheme.Throw

final case class Query private[capturl] (value: String)

object Query {

  val empty: Query = new Query("")

  def apply(query: String): Query = {
    QueryParser(query).phrase(_.iquery)
  }
}
