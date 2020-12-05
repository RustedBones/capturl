/*
 * Copyright 2019 Michel Davit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.davit.capturl.scaladsl

import fr.davit.capturl.ccompat.{Builder, QuerySeqOptimized}
import fr.davit.capturl.javadsl
import fr.davit.capturl.javadsl.QueryParameter
import fr.davit.capturl.parsers.QueryParser

import scala.collection.mutable
import scala.util.Try
import scala.jdk.CollectionConverters._

sealed abstract class Query extends javadsl.Query with QuerySeqOptimized {

  override def toString: String = {
    map {
      case (k, None)    => k
      case (k, Some(v)) => s"$k=$v"
    }.mkString("&")
  }

  /* Java API */
  override def getParameters(): java.lang.Iterable[QueryParameter] = {
    map {
      case (k, v) => new QueryParameter(k, v.orNull)
    }.asJava
  }
  override def asScala(): Query = this
}

object Query {

  val empty: Query = Empty

  def apply(query: String): Query = parse(query).get

  def parse(query: String): Try[Query] = {
    QueryParser(query).phrase(_.iquery)
  }

  def apply(query: Seq[(String, Option[String])]): Query = {
    query.foldRight(empty) { case ((k, v), q) => Part(k, v, q) }
  }

  def newBuilder: mutable.Builder[(String, Option[String]), Query] =
    new Builder[(String, Option[String]), Query] {
      val b                                                          = Seq.newBuilder[(String, Option[String])]
      override def addOne(elem: (String, Option[String])): this.type = { b += elem; this }
      def clear()                                                    = b.clear()
      def result()                                                   = apply(b.result())
    }

  //--------------------------------------------------------------------------------------------------------------------
  // Part
  //--------------------------------------------------------------------------------------------------------------------
  final case class Part(key: String, value: Option[String] = None, override val tail: Query = Empty) extends Query {
    override def isEmpty: Boolean               = false
    override def head: (String, Option[String]) = key -> value
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
