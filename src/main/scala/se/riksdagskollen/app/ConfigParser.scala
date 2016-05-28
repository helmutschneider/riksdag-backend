package se.riksdagskollen.app

import java.io.File

import scala.io.Source
import scala.util.matching.Regex

class ConfigParser {

  // matches KEY="VALUE"
  val matcher = new Regex("(\\w+)=\"([^\"]+)\"", "key", "value")

  def parse(data: String): Map[String, String] = {
    (matcher.findAllMatchIn(data) map { m =>
      (m.group("key"), m.group("value"))
    }).toMap
  }

  def parse(file: File): Map[String, String] = {
    val data = Source.fromFile(file).mkString
    parse(data)
  }

}
