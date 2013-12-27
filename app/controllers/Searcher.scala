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
import org.pegdown.{ PegDownProcessor, Extensions }
import org.sphx.api.SphinxClient
import scala.collection.mutable.HashMap
import org.apache.commons.lang3.StringEscapeUtils.escapeHtml4
import java.lang.NullPointerException
import play.api.libs.json.Json.toJson
import org.sphx.api.SphinxException
import play.Configuration.root
import play.Logger

object Searcher extends Controller {

  private val sphinx = new SphinxClient
  sphinx.SetServer(root.getString("sphinxServer"), root.getInt("sphinxPort"))

  class Result {
    private var id: Long = 0
    private var header: String = null
    private var snippets = List[String]()
    private var link: String = null

    def getId = id

    def getHeader = header

    def getSnippets = snippets

    def getLink = link

    def setId(value: Long) = id = value

    def setHeader(value: String) = header = value

    def setSnippets(value: List[String]) = snippets = value

    def setLink(value: String) = link = value
  }

  private def getResults(keywords: String, offset: Int, limit: Int) = {
    sphinx.SetLimits(offset, limit)
    val docIds = try {
      sphinx.Query(keywords, root.getString("indexName")).matches.map { _.docId }
    } catch {
      case e: NullPointerException => Array()
    }

    var results: Array[Result] = Array()

    DB.withConnection { implicit connection =>
      results = docIds.map { docId =>
        val result = new Result

        result.setId(docId)
        val prepareResult = SQL("SELECT url, header, content FROM files WHERE id = {id}").on("id" -> docId)()
        result.setHeader(prepareResult.map { _[String]("header") }.mkString)
        result.setLink(prepareResult.map { _[String]("url") }.mkString)

        val docs = prepareResult.map { content =>
          escapeHtml4(content[String]("content"))
        }.toArray[String]

        result.setSnippets(try {
          sphinx.BuildExcerpts(docs, root.getString("indexName"), keywords, HashMap[String, Int]("around" -> root.getInt("countSnippets"))).toList

        } catch {
          case e: SphinxException => List[String]()
        })

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
    Ok(views.html.search(results, keywords))
  }

  def searchJson(keywords: String, offset: Int, limit: Int) = Action {
    val results = toJson(Map("results" ->
      getResults(keywords, offset, limit).map { result =>
        toJson(Map(
          "id" -> toJson(result.getId),
          "header" -> toJson(result.getHeader),
          "snippets" -> toJson(result.getSnippets),
          "link" -> toJson(result.getLink)))
      }.toList))
    Ok(toJson(results))
  }
}
