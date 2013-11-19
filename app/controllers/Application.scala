package controllers

import play.api._
import play.api.mvc._
import java.io.File
import scala.io.Source
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.FileUtils
import scala.collection.JavaConversions._
import org.pegdown.{PegDownProcessor, Extensions}

object Application extends Controller {

  def update() = Action {
    val processor = new PegDownProcessor(Extensions.ALL)
    val mdFiles = FileUtils.iterateFiles(new File("../limb"), Array("md"), true)
    Ok(views.html.update(mdFiles.map { file =>
      processor.parseMarkdown(Source.fromFile(file.toString).mkString.toCharArray)
    }))
  }

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def search(keywords: String) = Action {
    Ok(views.html.search(keywords))
  }


}
