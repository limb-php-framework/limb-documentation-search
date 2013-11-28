package controllers

import anorm._
import play.api.Play.current
import play.api.db._
import play.api._
import play.api.mvc._
import java.io.File
import scala.io.Source
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.FileUtils
import scala.collection.JavaConversions._
import org.pegdown.{PegDownProcessor, Extensions}
import org.sphx.api.SphinxClient

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def search(keywords: String) = Action {
    val sphinx = new SphinxClient
    sphinx.SetServer("localhost", 9312)
    val statistics = sphinx.BuildKeywords(keywords, "limb", true)
    var results = List[String]()
    DB.getConnection()
    DB.withConnection{ implicit connection =>
      results = SQL("""SELECT url
        FROM id_url
        WHERE id IN (""" + sphinx.Query(keywords, "limb").matches.map{_.docId}.mkString(", ") + ");")().map {
        _[String]("url")
      }.toList
    }
    Ok(views.html.search(results, statistics))
  }
}
