package fr.davit.capturl

final case class Scheme(name: String)

object Scheme {
  // some predefined schemes
  val File = Scheme("file")
  val Http = Scheme("http")
  val Https = Scheme("https")
}
