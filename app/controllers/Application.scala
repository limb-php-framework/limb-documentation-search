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
import scala.collection.mutable.HashMap
import org.apache.commons.lang3.StringEscapeUtils.escapeHtml4

object Application extends Controller {

  class Result {
    var id: Long = 0
    var header = ""
    var snippets = Array[String]()
    var link = ""
  }

  def index = Action {
    Ok(views.html.index())
  }

  def search(keywords: String, offset: Int, limit: Int) = Action {
    val sphinx = new SphinxClient
    sphinx.SetServer("localhost", 9312)
    sphinx.SetLimits(offset, limit)
    val statistics = sphinx.BuildKeywords(keywords, "limb", true)
    val snippets = sphinx.BuildExcerpts(keywords.split(" "), "limb", "php", HashMap[String, Int]()).toString
    val docIds = sphinx.Query(keywords, "limb").matches.map{_.docId}
    DB.getConnection()
    var results: Array[Result] = Array()
    DB.withConnection{ implicit connection =>
      results = docIds.map { docId =>
        val result = new Result
        result.id = docId
        val prepareResult = SQL("SELECT url, header, content FROM id_url WHERE id = " + docId + ";")()
        result.header = prepareResult.map {
          _[String]("header")
        }.mkString
        result.link = prepareResult.map {
          _[String]("url")
        }.mkString
        val docs = prepareResult.map{ content =>
          escapeHtml4( content[String]("content"))
        }.toArray[String]
        result.snippets = sphinx.BuildExcerpts(docs, "limb", keywords, HashMap[String, Int]("around" -> 10))
        result
      }
      // links = SQL("""SELECT url
      //   FROM id_url
      //   WHERE id IN (""" + docIds.mkString(", ") + ");")().map {
      //   _[String]("url")
      // }.toList
    }
    Ok(views.html.search(results, statistics))
  }
}
