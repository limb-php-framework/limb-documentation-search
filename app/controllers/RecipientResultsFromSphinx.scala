package controllers

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging
import scala.concurrent.{ ExecutionContext, Future }
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.actor._
import org.sphx.api.{ SphinxClient, SphinxException }
import play.Configuration.root
import scala.collection.mutable.HashMap
import scala.collection.JavaConversions._

class PageElement {

  var docs: Array[String] = Array()

  var keywords: String = ""

}


class RecipientResultsFromSphinx extends Actor with ActorLogging {

  val sphinx = new SphinxClient
  sphinx.SetServer(root.getString("sphinx_server"), root.getInt("sphinx_port"))

  sphinx.SetFieldWeights(HashMap(
    "header1" -> 1000,
    "header2" -> 900,
    "header3" -> 800,
    "header4" -> 700,
    "header5" -> 600,
    "header6" -> 500))

  sphinx.SetLimits(1, root.getInt("page_results"))

  def getSphinxStatus = {
    sphinx.Open
    sphinx.Close
    !sphinx.IsConnectError
  }

  def receive = {
    case pageElement: PageElement => sender ! sphinx.BuildExcerpts(pageElement.docs, root.getString("index_name"), pageElement.keywords, HashMap[String, Int]("around" -> root.getInt("count_snippets"))).toList
    case "sphinxStatus" => sender ! getSphinxStatus
    case keywords: String => sender ! sphinx.Query(keywords, root.getString("index_name"))
    case page: Int => sphinx.SetLimits(root.getInt("page_results") * page - root.getInt("page_results"),
      root.getInt("page_results"))
 }

}
