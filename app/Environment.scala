package app

import java.io.File

import scala.collection.mutable
import scala.io.Source

/**
 * Created by Johan on 2015-08-16.
 */
object Environment {

  private val env = mutable.Map[String, String]()

  def getOrElse(key: String, orElse: String): String = {
    env.getOrElse(key, orElse)
  }

  def parseEnvFile(file: File) {
    env.clear()
    val lines = Source.fromFile(file).getLines()
    for ( line <- lines ) {

      // valid lines look like KEY=VALUE
      if ( line.matches(".+=.+") ) {
        val parts = line.split("=").map(p => p.trim())
        env.+=(parts(0) -> parts(1))
      }
    }

  }

}
