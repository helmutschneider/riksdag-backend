package se.riksdagskollen.app

import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json._

trait Servlet extends ScalatraServlet with JacksonJsonSupport {

  // Sets up automatic case class to JSON output serialization, required by
  // the JValueResult trait.
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  // Before every action runs, set the content type to be in JSON format.
  before() {
    contentType = formats("json")
  }

  notFound {
    // remove content type in case it was set through an action
    contentType = null
    // Try to render a ScalateTemplate if no route matched
    serveStaticResource() getOrElse resourceNotFound()
  }

}
