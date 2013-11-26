package controllers

import play.api._
import play.api.mvc._
import java.io.File
import scala.io.Source
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.FileUtils
import scala.collection.JavaConversions._
import org.pegdown.{PegDownProcessor, Extensions}
import org.sphx.api.SphinxClient
import com.sphinxsearch.indexer.{Document, Index, IndexDescription}

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def search(keywords: String) = Action {
    Ok(views.html.search(keywords))
  }
}
