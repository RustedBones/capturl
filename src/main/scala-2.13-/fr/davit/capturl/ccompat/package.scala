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
import scala.collection.generic.{CanBuildFrom, GenericCompanion}
import scala.collection.{immutable, mutable, GenTraversable}
import scala.{collection => c}

package object ccompat {
  import CompatImpl._

  implicit def genericCompanionToCBF[A, CC[X] <: GenTraversable[X]](
      fact: GenericCompanion[CC]
  ): CanBuildFrom[Any, A, CC[A]] =
    simpleCBF(fact.newBuilder[A])

  // This really belongs into scala.collection but there's already a package object
  // in scala-library so we can't add to it
  type IterableOnce[+X] = c.TraversableOnce[X]
  val IterableOnce = c.TraversableOnce
}

package ccompat {

  trait Builder[-Elem, +To] extends mutable.Builder[Elem, To] { self =>
    // This became final in 2.13 so cannot be overridden there anymore
    final override def +=(elem: Elem): this.type = addOne(elem)
    def addOne(elem: Elem): this.type            = self.+=(elem)
  }

  trait QuerySeqOptimized
      extends immutable.LinearSeq[(String, Option[String])]
      with c.LinearSeqOptimized[(String, Option[String]), Query] { self: Query =>
    override def newBuilder: mutable.Builder[(String, Option[String]), Query] = Query.newBuilder
  }
}
