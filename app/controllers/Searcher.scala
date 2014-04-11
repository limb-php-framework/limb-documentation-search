package controllers

import anorm._
import anorm.SqlParser.get
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
import org.sphx.api.{ SphinxClient, SphinxException }
import scala.collection.mutable.HashMap
import org.apache.commons.lang3.StringEscapeUtils.escapeHtml4
import java.lang.NullPointerException
import play.api.libs.json.Json.toJson
import play.Configuration.root
import sys.process._
import java.sql.SQLException
import akka.actor.Props
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.routing.FromConfig
import play.api.libs.concurrent._
import scala.concurrent.Future


object Searcher extends Controller {

  val actorSystem = ActorSystem("system")
  implicit val timeout = Timeout(root.getMilliseconds("future_timeout"))

  val sphinx = actorSystem.actorOf(Props[RecipientResultsFromSphinx].withRouter(FromConfig()), "sphinx-router")

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

  class Results {
    private var results: Array[Result] = Array()
    private var found: Int = 0
    private var keywords: String = ""
    private var page: Int = 1

    def getResults = results

    def setResults(value: Array[Result]) = results = value

    def getFound = found

    def setFound(value: Int) = found = value

    def getKeywords = keywords

    def setKeywords(value: String) = keywords = value

    def getPage = page

    def setPage(value: Int) = page = value

    def getPageCount: Int = {
      val entirePages = (found / root.getInt("page_results"))
      if (found > entirePages * root.getInt("page_results")) {
        return entirePages + 1
      }
      return entirePages
    }
  }

  private def getResults(keywords: String, page: Int): Results = {

    val request = new ActorRequest()

    request.page = page
    request.keywords = keywords

    val queryResults = (Await.result(sphinx ? request, Duration.Inf)).asInstanceOf[org.sphx.api.SphinxResult]

    if (queryResults == null) {
      Logger("application").error("Failed to connect to Sphinx or error retrieving results from the Sphinx")
      return null
    }

    val docIds = queryResults.matches.map { _.docId }

    var results = new Results
    results.setKeywords(keywords)
    results.setFound(queryResults.totalFound)
    results.setPage(page)
    try {
      DB.withConnection { implicit connection =>

        object ViewResult {
          val parser = {
            get[Long]("id") ~
            get[String]("url") ~
            get[String]("header1") ~
            get[String]("content") map {
              case id ~ url ~ header1 ~ content => val result = new Result
                val docs = escapeHtml4(content)
                result.setId(id)
                result.setHeader(header1)
                val pageElement = new PageElement
                pageElement.docs = Array(docs)
                pageElement.keywords = keywords
                val snippets = (Await.result(sphinx ? pageElement, Duration.Inf)).asInstanceOf[List[String]]
                result.setSnippets(snippets)
                result.setLink(url)
                result
            }
          }
        }
        if (docIds.length > 0) {
          results.setResults(SQL("SELECT id, url, header1, content FROM files WHERE id in ( " + docIds.mkString(", ") + " );").as(ViewResult.parser *).toArray)
        }
        results.setResults(docIds.map { docId =>
	  results.getResults.filter{ _.getId == docId }(0)
	})
      }
    } catch {
      case e: SQLException => Logger("application").error("Failed to retrieve data from the database")
        return null
    }

    results
  }

  def index = Action {
    Ok(views.html.index())
  }

  def search(keywords: String, page: Int) = Action.async {
    if (keywords.length == 0) {
      Future {
        Ok( views.html.index())
      }
    } else {
      val results =  getResults(keywords, page)
      if (results == null) {
        Future {
          InternalServerError(views.html.internalError())
        }
      } else {
        Future {
          Ok(views.html.search(results))
        }
      }
    }
  }

  def searchJson(keywords: String, page: Int) = Action.async {
    val prepareResults = getResults(keywords, page)
    if (prepareResults == null) {
      Future {
        InternalServerError(views.html.internalError())
      }
    } else {
      val results = Map("results" ->
        prepareResults.getResults.map { result =>
          toJson(Map(
            "id" -> toJson(result.getId),
            "header" -> toJson(result.getHeader),
            "snippets" -> toJson(result.getSnippets),
            "link" -> toJson(result.getLink)))
        }.toList)
      Future {
        Ok(toJson(results))
      }
    }
  }

  private def getDebDateBuild = {
    scala.io.Source.fromFile("/var/lib/limb-docs-searcher/datebuild").
      mkString.
      replaceAll("""(?m)\s+$""", "") // strip
  }

  private def getDebVersion = {
    ("apt-cache policy limb-docs-searcher" #| "head -n 2" #| "tail -n 1" !!).
      split(" ")(3).replaceAll("""(?m)\s+$""", "")
  }

  private def getSphinxStatus = {

    val status = (Await.result((sphinx ? "sphinxStatus"), Duration.Inf)).asInstanceOf[Boolean]

    if (status) {
      "success"
    } else {
      Logger("application").error("Failed to connect to Sphinx")
      "failed"
    }
  }

  private def getDBStatus = {
    var dbConn: String = "success"

    try {
      DB.withConnection { implicit connection =>
        SQL("SELECT 1")()
      }
    } catch {
      case e: java.sql.SQLException => dbConn = "failed"
    }

    dbConn
  }

  def status = Action {
    Ok(views.xml.status(getDebDateBuild, getDebVersion, getDBStatus, getSphinxStatus))
  }
}
