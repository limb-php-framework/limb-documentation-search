package controllers

import play.api.Play.current
import play.api.db._
import play.api._
import play.api.mvc._
import anorm._
import java.sql.DriverManager
import org.streum.configrity._
import org.pegdown.{ PegDownProcessor, Extensions }
import org.pegdown.ast.{ TextNode, RootNode, HeaderNode, Node }
import org.apache.commons.io.FileUtils
import java.io.File
import scala.collection.JavaConversions._
import scala.io.Source
import scala.collection.mutable.{ ArrayBuffer, Buffer }
import sys.process._
import java.util.Date
import java.text.SimpleDateFormat
import play.Configuration.root
import java.nio.charset.MalformedInputException
import org.apache.commons.io.FilenameUtils

object Indexer extends Controller {

  class SphinxElement {

    private var header: String = null
    private var content: String = null
    private var url: String = null

    def getHeader: String = {
      header
    }

    def getContent: String = {
      content
    }

    def getURL: String = {
      url
    }

    def addHeader(value: String) = {
      header += " " + value
    }

    def addContent(value: String) = {
      content += " " + value
    }

    def setURL(value: String) = {
      url = value
    }
  }

  private val devNull = new File("/dev/null")

  private def cloningRepository = {
    Seq("git clone", root.getString("limb_git_path"), root.getString("limb_local_path")).mkString(" ") #> devNull !
  }

  private def updatingRepository = {
    Seq("cd", root.getString("limb_git_path"), "&& git pull").mkString(" ") #> devNull !
  }

  private def initRepository = {
    if (root.getBoolean("download_limb_if_not_exists")) {
      if (!(new File(root.getString("limb_local_path"))).exists) {
        val status = cloningRepository

        if (status == 0 ) {
          Logger("application").info("Cloned repository")
        } else {
          Logger("application").error("Failed to clone the repository. Status code " + status)
        }
      }
    } else {
      if (root.getBoolean("update_limb_local_repo")) {
        val status = updatingRepository

        if (status == 0 ) {
          Logger("application").info("Cloned repository")
        } else {
          Logger("application").error("Failed to update the repository. Status code " + status)
        }
      }
    }
  }

  private def omgGetTextFromTextNode(node: Node): String = {
    node.asInstanceOf[TextNode].getText
  }

  private def getTextFromHeaderNode(node: Node): String = {
    node.getChildren.map { subNode =>
      if (subNode.getClass == classOf[TextNode]) {
        omgGetTextFromTextNode(subNode)
      }
    }.filter { _.getClass == classOf[String] }.mkString(" ")
  }

  private def getMdFiles(limbDirectory: File, date: Date): List[File] = {
    val dateFormat = new SimpleDateFormat("MMMhh:mm:ss'MSK'y-HH:mm:ss")
    val cmd = Seq("git --git-dir=", limbDirectory.getAbsolutePath, """/.git log --since="""" + dateFormat.format(date) + "\" --name-only --pretty=format:").mkString
    val grep = "grep md"
    val prepareFiles: java.lang.String = try { (cmd #| grep !!) } catch { case e: java.lang.RuntimeException => return List[File]() }
    prepareFiles.split("\n").map { path =>
      new File(Seq(limbDirectory.getAbsolutePath, path).mkString("/"))
    }.filter { file => "md" == FilenameUtils.getExtension(file.getAbsolutePath) && file.exists }.toList
  }

  private def indexation = {
    new Thread(new Runnable {
      def run() {
        initRepository
        val processor = new PegDownProcessor(Extensions.ALL)
        val limbDirectory = new File(root.getString("limb_local_path"))
        var updatedDate = new Date(0)
        val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        DB.withConnection { implicit connection =>
          SQL("SELECT to_char(timestamp, 'YYYY-MM-DD HH:MM:SS') as timestamp FROM updates")().foreach { row =>
            updatedDate = dateFormat.parse(row[String]("timestamp"))
          }
        }
        getMdFiles(limbDirectory, updatedDate).foreach { file =>
          val source = Source.fromFile(file).mkString.toCharArray
          val mdTree = processor.parseMarkdown(source)
          val element = new SphinxElement
          val fileURL = (new File(root.getString("limb_local_path")).toURI.relativize((new File(file.toString)).toURI)).getPath
          element.setURL(fileURL)
          var firstHeader = true
          def getTextFromOtherNodes(nodeList: java.util.List[Node]): Unit = {
            nodeList.foreach { node =>
              node match {
                case _: TextNode => element.addContent(omgGetTextFromTextNode(node))
                case _: HeaderNode => if (firstHeader) {
                  element.addHeader(getTextFromHeaderNode(node))
                  firstHeader = false
                  getTextFromOtherNodes(node.getChildren)
                }
                case _ => getTextFromOtherNodes(node.getChildren)
              }
            }
          }
          getTextFromOtherNodes(mdTree.getChildren)
          DB.withConnection { implicit connection =>
            if (SQL("SELECT url FROM files WHERE url = {url}").on("url" -> element.getURL)().map { _[String]("url") }.mkString.length > 0) {
              SQL("UPDATE files SET url={url}, header={header}, content={content} WHERE url={url}").on(
                "url" -> element.getURL,
                "header" -> element.getHeader,
                "content" -> element.getContent).executeUpdate()
            } else {
              SQL("INSERT INTO files (url, header, content) VALUES ({url}, {header}, {content})").on(
                "url" -> element.getURL,
                "header" -> element.getHeader,
                "content" -> element.getContent).execute()
            }
          }
        }
        DB.withConnection { implicit connection =>
          SQL("UPDATE updates SET id = {id}, timestamp = NOW()").on("id" -> Id).executeUpdate()
        }
      }
    })
  }

  def update(token: String) = Action {
    if (token != root.getString("secret_token_for_indexing")) {
      Forbidden("No access")
    } else {
      indexation.start
      Ok("Started indexing")
    }
  }

}
