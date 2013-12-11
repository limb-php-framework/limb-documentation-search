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
import java.lang.NullPointerException
import play.api.libs.json.Json.toJson
import org.sphx.api.SphinxException
import play.Configuration.root
import play.Logger


object Application extends Controller {

  private val sphinx = new SphinxClient
  sphinx.SetServer(root.getString("sphinxServer"), root.getInt("sphinxPort"))

  class Result {
    var id: Long = 0
    var header = ""
    var snippets = List[String]()
    var link = ""
  }

  private def getResults(keywords: String, offset: Int, limit: Int) = {
    sphinx.SetLimits(offset, limit)
    val docIds = try {
      sphinx.Query(keywords, "limb").matches.map{_.docId}
    } catch {
      case e: NullPointerException => Array()
    }
    var results: Array[Result] = Array()
    DB.withConnection{ implicit connection =>
      results = docIds.map { docId =>
        val result = new Result
        result.id = docId
        val prepareResult = SQL("SELECT url, header, content FROM id_url WHERE id = {id}").on("id" -> docId)()
        result.header = prepareResult.map {
          _[String]("header")
        }.mkString
        result.link = prepareResult.map {
          _[String]("url")
        }.mkString
        val docs = prepareResult.map{ content =>
          escapeHtml4( content[String]("content"))
        }.toArray[String]
        result.snippets = try {
          sphinx.BuildExcerpts(docs, "limb", keywords, HashMap[String, Int]("around" -> 15)).toList
        } catch {
          case e: SphinxException =>  List[String]()
        }
        result
      }
    }
    results
  }

  def index = Action {
    Ok(views.html.index())
  }

  def search(keywords: String, offset: Int, limit: Int, autoload: Boolean) = Action {
    val results = getResults(keywords, offset, limit)
    Ok(views.html.search(results))
  }

  def searchJson(keywords: String, offset: Int, limit: Int) = Action {
    val results = toJson(Map("results" ->
      getResults(keywords, offset, limit).map { result =>
        toJson(Map(
          "id" -> toJson(result.id),
          "header" -> toJson(result.header),
          "snippets" -> toJson(result.snippets),
          "link" -> toJson(result.link)
        ))
      }.toList))
    Ok(toJson(results))
  }
}
