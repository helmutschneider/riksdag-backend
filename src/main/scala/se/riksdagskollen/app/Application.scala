package se.riksdagskollen.app

import java.nio.file.Path
import javax.sql.DataSource

import se.riksdagskollen.http.HttpClientTrait

trait Application {
  def root: Path
  def config: Map[String, String]
  def version: String
  def dataSource: DataSource
  def httpClient: HttpClientTrait
}
