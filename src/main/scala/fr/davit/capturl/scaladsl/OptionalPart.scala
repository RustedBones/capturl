package fr.davit.capturl.scaladsl

trait OptionalPart[+T] {
  def toOption: Option[T]

  def isEmpty: Boolean
  final def nonEmpty: Boolean = !isEmpty
}

object OptionalPart {

  trait DefinedPart[T] extends OptionalPart[T] {
    protected def value: T

    override def toOption: Option[T] = Some(value)
    override def isEmpty: Boolean = false
    override def toString: String = value.toString
  }

  trait EmptyPart extends OptionalPart[Nothing] {
    override def toOption: Option[Nothing] = None
    override def isEmpty: Boolean = true
    override def toString: String = ""
  }
}
