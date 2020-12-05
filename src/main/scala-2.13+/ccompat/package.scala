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

package fr.davit.capturl

import fr.davit.capturl.scaladsl.Query
import scala.collection.immutable

package object ccompat {
  type Builder[-A, +To] = scala.collection.mutable.Builder[A, To]
}

package ccompat {

  trait QuerySeqOptimized
      extends immutable.LinearSeq[(String, Option[String])]
      with scala.collection.StrictOptimizedLinearSeqOps[(String, Option[String]), immutable.LinearSeq, Query] {
    self: Query =>

    override protected def fromSpecific(coll: IterableOnce[(String, Option[String])]): Query =
      Query(coll.iterator.to(Seq))

    override protected def newSpecificBuilder: Builder[(String, Option[String]), Query] = Query.newBuilder

    override def empty: Query = Query.Empty
  }

}
