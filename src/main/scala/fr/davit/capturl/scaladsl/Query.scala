package fr.davit.capturl.scaladsl

import fr.davit.capturl.parsers.QueryParser
import org.parboiled2.Parser.DeliveryScheme.Throw
import fr.davit.capturl.javadsl
import fr.davit.capturl.javadsl.QueryParameter

import scala.collection.immutable.LinearSeq
import scala.collection.{mutable, LinearSeqOptimized}
import scala.collection.JavaConverters._

sealed abstract class Query
    extends javadsl.Query
    with LinearSeq[(String, Option[String])]
    with LinearSeqOptimized[(String, Option[String]), Query] {
  override def newBuilder: mutable.Builder[(String, Option[String]), Query] = Query.newBuilder
  override def toString: String = mkString("&")

  /* Java API */
  override def getParameters(): java.lang.Iterable[QueryParameter] =
    map { case (k, v) => new QueryParameter(k, v.orNull) }.asJava
  override def asScala(): Query = this
}

object Query {

  val empty: Query = Empty

  def apply(query: String): Query = {
    QueryParser(query).phrase(_.iquery)
  }

  def apply(query: Seq[(String, Option[String])]): Query = {
    query.foldLeft(empty) { case (q, (k, v)) => Part(k, v, q) }
  }

  def newBuilder: mutable.Builder[(String, Option[String]), Query] =
    new mutable.Builder[(String, Option[String]), Query] {
      val b                                                          = Seq.newBuilder[(String, Option[String])]
      def +=(elem: (String, Option[String])): this.type = { b += elem; this }
      def clear()                                                    = b.clear()
      def result()                                                   = apply(b.result())
    }

  //--------------------------------------------------------------------------------------------------------------------
  // Part
  //--------------------------------------------------------------------------------------------------------------------
  final case class Part(key: String, value: Option[String] = None, override val tail: Query = Empty) extends Query {
    override def isEmpty: Boolean               = false
    override def head: (String, Option[String]) = key -> value
    override def toString: String = value match {
      case Some(v) => s"$key=$v"
      case None    => key
    }
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Empty
  //--------------------------------------------------------------------------------------------------------------------
  case object Empty extends Query {
    override def isEmpty: Boolean               = true
    override def head: (String, Option[String]) = throw new NoSuchElementException("head of empty query")
    override def tail: Query                    = throw new UnsupportedOperationException("tail of empty query")
    override def toString: String               = ""
  }

}
