package controllers

import play.api.Play.current
import play.api.db._
import play.api._
import play.api.mvc._
import anorm._
import java.sql.DriverManager
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
import java.util.Date

object Indexer extends Controller {

  class SphinxElement {

    private var header1: Array[String] = Array()
    private var header2: Array[String] = Array()
    private var header3: Array[String] = Array()
    private var header4: Array[String] = Array()
    private var header5: Array[String] = Array()
    private var header6: Array[String] = Array()
    private var content: String = ""
    private var url: String = ""

    def getHeader(level: Int): Array[String] = {
      level match {
        case 1 => header1
        case 2 => header2
        case 3 => header3
        case 4 => header4
        case 5 => header5
        case 6 => header6
      }
    }

    def getContent: String = {
      content
    }

    def getURL: String = {
      url
    }

    def addHeader(value: String, level: Int) = {
      level match {
        case 1 => header1 = header1 :+ value
        case 2 => header2 = header2 :+ value
        case 3 => header3 = header3 :+ value
        case 4 => header4 = header4 :+ value
        case 5 => header5 = header5 :+ value
        case 6 => header6 = header6 :+ value
      }
    }

    def addContent(value: String) = {
      content += " " + value
    }

    def setURL(value: String) = {
      url = value
    }
  }

  private val devNull = new File("/dev/null")

  def shellCommandExecute(args: String*) = args.mkString(" ") #> devNull !

  def shellCommandExecuteGrep(grep: String, args: String*) = {
    println(args.mkString(" "))
    args.mkString(" ") #| grep !!
  }

  private def cloningRepository = {
    shellCommandExecute("git clone", root.getString("limb_git_path"), root.getString("limb_local_path"))
  }

  private def updatingRepository = {
    shellCommandExecute("cd", root.getString("limb_git_path"), "&& git pull")
  }

  private def initRepository = {
    if (root.getBoolean("download_limb_if_not_exists")) {
      if (!(new File(root.getString("limb_local_path"))).exists) {
        val status = cloningRepository

        if (status == 0) {
          Logger("application").info("Cloned repository")
        } else {
          Logger("application").error("Failed to clone the repository. Status code " + status)
        }
      }
    } else {
      if (root.getBoolean("update_limb_local_repo")) {
        val status = updatingRepository

        if (status == 0) {
          Logger("application").info("Cloned repository")
        } else {
          Logger("application").error("Failed to update the repository. Status code " + status)
        }
      }
    }
  }

  private def mdFilesFilter(limbDirectory: File, prepareFiles: Array[String]): List[File] = {
    prepareFiles.map { path =>
      new File(Seq(limbDirectory.getAbsolutePath, path).mkString("/"))
    }.filter { file =>
      "md" == FilenameUtils.getExtension(file.getAbsolutePath) && file.exists
    }.toList
  }

  private def removeDeletedMdFiles(limbDirectory: File, date: Date) {
    val files = shellCommandExecuteGrep("grep md", "git --git-dir=" + limbDirectory.getAbsolutePath + """/.git""", "log", """--since="""" + getDateForGit(date) + "\"", "--name-only", "--pretty=format:", "--diff-filter=D")
    DB.withConnection { implicit connection =>
      for(url <- mdFilesFilter(limbDirectory, files.split("\n"))) {
        SQL("DELETE FROM files WHERE url={url}").on("url" -> getFileURL(url)).execute()
      }
    }
  }

  // encapsulation incomprehensible moment, because of which it is necessary to do asInstanceOf byd method getText
  private def getTextFromTextNode(node: Node): String = {
    node.asInstanceOf[TextNode].getText
  }

  private def getTextFromHeaderNode(node: Node): String = {
    node.getChildren.map { subNode =>
      if (subNode.getClass == classOf[TextNode]) {
        getTextFromTextNode(subNode)
      }
    }.filter { _.getClass == classOf[String] }.mkString(" ")
  }

  private def getDateForGit(date: Date) = {
    val dateFormat = new SimpleDateFormat("MMMhh:mm:ss'MSK'y-HH:mm:ss")
    dateFormat.format(date)
  }

  private def getMdFiles(limbDirectory: File, date: Date): List[File] = {
    val prepareFiles = try {
      shellCommandExecuteGrep("grep md", "git --git-dir=" + limbDirectory.getAbsolutePath + """/.git log --since="""" + getDateForGit(date) + "\" --name-only --pretty=format:")
    } catch {
      case e: java.lang.RuntimeException => return List[File]()
    }
    mdFilesFilter(limbDirectory, prepareFiles.split("\n"))
  }

  private def getUpdatedDate = {
    val file = new File(root.getString("last_modified_file"))
    new Date(file.lastModified)
  }

  private def getMdTree(file: File) = {
    val processor = new PegDownProcessor(Extensions.ALL)
    val source = Source.fromFile(file).mkString.toCharArray
    processor.parseMarkdown(source)
  }

  private def getFileURL(file: File) = {
    (new File(root.getString("limb_local_path")).toURI.relativize((new File(file.toString)).toURI)).getPath
  }

  private def saveTree(element: SphinxElement): Unit = {
    DB.withConnection { implicit connection =>
      if (SQL("SELECT url FROM files WHERE url = {url}").on("url" -> element.getURL)().map { _[String]("url") }.mkString.length > 0) {
        SQL("UPDATE files SET url={url}, header1={header1}, header2={header2}, header3={header3}, header4={header4}, header5={header5}, header6={header6}, content={content} WHERE url={url}").on(
          "url" -> element.getURL,
          "header1" -> element.getHeader(1).mkString(" "),
          "header2" -> element.getHeader(2).mkString(" "),
          "header3" -> element.getHeader(3).mkString(" "),
          "header4" -> element.getHeader(4).mkString(" "),
          "header5" -> element.getHeader(5).mkString(" "),
          "header6" -> element.getHeader(6).mkString(" "),
          "content" -> element.getContent).executeUpdate()
      } else {
        SQL("INSERT INTO files (url, header1, header2, header3, header4, header5, header6, content) VALUES ({url}, {header1}, {header2}, {header3}, {header4}, {header5}, {header6}, {content})").on(
          "url" -> element.getURL,
          "header1" -> element.getHeader(1).mkString(" "),
          "header2" -> element.getHeader(2).mkString(" "),
          "header3" -> element.getHeader(3).mkString(" "),
          "header4" -> element.getHeader(4).mkString(" "),
          "header5" -> element.getHeader(5).mkString(" "),
          "header6" -> element.getHeader(6).mkString(" "),
          "content" -> element.getContent).execute()
      }
    }
  }

  private def updateDate: Unit = {
    val file = new File(root.getString("last_modified_file"))
    if (file.exists) {
      val status = file.setLastModified(System.currentTimeMillis)
      if (!status) {
        Logger("application").warn("Failed to install the update. Check permissions to last_modified_file.")
      }
    } else {
      val status = file.createNewFile
      if (!status) {
        Logger("application").warn("Error creating last_modified_file.")
      }
    }
  }

  private def indexation = {
    new Thread(new Runnable {
      def run() {
        initRepository
        val limbDirectory = new File(root.getString("limb_local_path"))
        var updatedDate = getUpdatedDate
        removeDeletedMdFiles(limbDirectory, updatedDate)
        Logger("application").info("Complete database started")
        getMdFiles(limbDirectory, updatedDate).foreach { file =>
          Logger("application").debug("Handling " + file.getAbsolutePath)
          val mdTree = getMdTree(file)
          val element = new SphinxElement
          val fileURL = getFileURL(file)
          element.setURL(fileURL)
          def getTextFromOtherNodes(nodeList: java.util.List[Node]): Unit = {
            nodeList.foreach { node =>
              node match {
                case _: TextNode => element.addContent(getTextFromTextNode(node))
                case _: HeaderNode => element.addHeader(getTextFromHeaderNode(node), node.asInstanceOf[HeaderNode].getLevel)
                case _ => getTextFromOtherNodes(node.getChildren)
              }
            }
          }
          getTextFromOtherNodes(mdTree.getChildren)
          if (!(element.getHeader(1).isEmpty || element.getContent.isEmpty)) {
            saveTree(element)
          }
        }
        updateDate
        Logger("application").info("Complete database completed")
      }
    })
  }

  def update = Action { request =>
    val token = request.body.asFormUrlEncoded.get("token").head
    if (token != root.getString("secret_token_for_indexing")) {
      Forbidden("Access denied")
    } else {
      indexation.start
      Ok("Starting parsing and write to database")
    }
  }

}
