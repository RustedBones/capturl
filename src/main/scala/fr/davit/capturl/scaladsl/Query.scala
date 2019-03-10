package fr.davit.capturl.scaladsl

import fr.davit.capturl.parsers.QueryParser
import org.parboiled2.Parser.DeliveryScheme.Throw

import scala.collection.immutable.LinearSeq
import scala.collection.{LinearSeqOptimized, mutable}

sealed trait Query extends LinearSeq[(String, String)] with LinearSeqOptimized[(String, String), Query] {
  override def newBuilder: mutable.Builder[(String, String), Query] = Query.newBuilder
  override def toString: String = mkString("&")
}

object Query {
  
  val empty: Query = Empty

  def apply(query: String): Query = {
    QueryParser(query).phrase(_.iquery)
  }

  def apply(query: Seq[(String, String)]): Query = {
    query.foldLeft(empty) { case (q, (k, v)) => Part(k, v, q) }
  }

  def newBuilder: mutable.Builder[(String, String), Query] = new mutable.Builder[(String, String), Query] {
    val b = Seq.newBuilder[(String, String)]
    def +=(elem: (String, String)): this.type = { b += elem; this }
    def clear() = b.clear()
    def result() = apply(b.result())
  }
  
  //--------------------------------------------------------------------------------------------------------------------
  // Part
  //--------------------------------------------------------------------------------------------------------------------
  final case class Part private[capturl] (key: String, value: String, override val tail: Query) extends Query {
    override def isEmpty: Boolean = false
    override def head: (String, String) = key -> value
    override def toString: String = if (value.isEmpty) key else s"$key=$value"
  }

  object Part {
    def apply(key: String, value: String, tail: Query): Part = {
      new Part(QueryParser(key).phrase(_.part), QueryParser(value).phrase(_.part), tail)
    }
  }

  //--------------------------------------------------------------------------------------------------------------------
  // Empty
  //--------------------------------------------------------------------------------------------------------------------
  case object Empty extends Query {
    override def isEmpty: Boolean = true
    override def head: (String, String) = throw new NoSuchElementException("head of empty query")
    override def tail: Query = throw new UnsupportedOperationException("tail of empty query")
    override def toString: String = ""
  }
  
}
