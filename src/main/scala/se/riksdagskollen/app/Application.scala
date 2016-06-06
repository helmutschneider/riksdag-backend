package se.riksdagskollen.app

import java.nio.file.Path
import javax.sql.DataSource
import scala.concurrent.ExecutionContext

trait Application {
  def root: Path
  def config: Map[String, String]
  def version: String
  def dataSource: DataSource
  def executionContext: ExecutionContext
}
