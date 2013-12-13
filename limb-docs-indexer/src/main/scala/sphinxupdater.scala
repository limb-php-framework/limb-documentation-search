package indexer

import java.sql.DriverManager
import org.streum.configrity._
import com.sphinxsearch.indexer.{ Document, Index, IndexDescription }
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

object xmlpipe2Generator {

  val config = Configuration.load("/etc/limb-docs-indexer/config")

  class SphinxElement {
    private var header: String = ""
    private var content: String = ""
    private var url: String = ""

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

  private def getMdFiles(limbDirectory: File, newDB: Boolean, date: Date): List[File] = {
    if (newDB) {
      FileUtils.iterateFiles(limbDirectory, Array("md"), true).map { file => file.asInstanceOf[File] }.toList
    } else {
      val dateFormat = new SimpleDateFormat("MMMhh:mm:ss'MSK'y")
      val cmd = Seq("git --git-dir=", limbDirectory.getAbsolutePath, """/.git log --since="""" + dateFormat.format(date) + "\" --name-only --pretty=format:").mkString
      val grep = "grep md"
      val prepareFiles: java.lang.String = try { (cmd #| grep !!) } catch { case e: java.lang.RuntimeException => return List[File]() }
      if (prepareFiles.length == 0) return List[File]()
      prepareFiles.split("\n").map { file =>
        new File(Seq(limbDirectory.getAbsolutePath, file).mkString("/"))
      }.toList
    }
  }

  private def getTextFromHeaderNode(node: Node): String = {
    node.getChildren.map { subNode =>
      if (subNode.getClass == classOf[TextNode]) {
        omgGetTextFromTextNode(subNode)
      }
    } filter { _.getClass == classOf[String] } mkString (" ")
  }

  private def omgGetTextFromTextNode(node: Node): String = {
    node.asInstanceOf[TextNode].getText
  }

  def main(args: Array[String]): Unit = {
    if (config[Boolean]("download_limb_if_not_exists")) {
      if (!(new File(config[String]("limb_local_path"))).exists) {
        val status = Array("git clone", config[String]("limb_git_path"), config[String]("limb_local_path")).mkString(" ") #> (new File("/dev/null")) !

        if (status != 0) {
          System.exit(status)
        }
      }
    } else {
      if (config[Boolean]("update_limb_local_repo")) {
        val status = ("cd " + config[String]("limb_git_path") + " && git pull ") #> (new File("/dev/null")) !
      }
    }
    Class.forName("org.sqlite.JDBC")
    val db_file = new File(config[String]("db_path"))
    var newDB: Boolean = true
    if (db_file.exists) {
      newDB = false
    }
    val connection = DriverManager.getConnection("jdbc:sqlite:" + config[String]("db_path"))
    val statement = connection.createStatement()
    var updatedDate = new Date(0)
    var id = 1
    if (newDB) {
      statement.executeUpdate("CREATE TABLE id_url (id INTEGER, url TEXT, header TEXT, content TEXT)")
      statement.executeUpdate("CREATE TABLE updates (id INTEGER PRIMARY KEY, timestamp TIMESTAMP DEFAULT (datetime('now','localtime')), start_id INTEGER, stop_id INTEGER)")
    } else {
      val results = statement.executeQuery("SELECT stop_id, timestamp FROM updates WHERE id=( SELECT max(id) FROM updates )")
      id = results.getInt("stop_id")
      val formattedDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
      updatedDate = formattedDate.parse(results.getString("timestamp"))
    }
    val processor = new PegDownProcessor(Extensions.ALL)
    val limbDirectory = new File(config[String]("limb_local_path"))
    val HEADER = "header"
    val CONTENT = "content"
    val URL = "url"
    val CODE = "code"
    val desc = IndexDescription.createIndexDescription(HEADER, CONTENT)
    desc.addStr2OrdinalAttribute(URL)
    val index = Index.createIndex(desc)
    val startId = id
    val mdFiles: List[File] = getMdFiles(limbDirectory, newDB, updatedDate)
    mdFiles.foreach { file =>
      val mdTree = processor.parseMarkdown(Source.fromFile(file.toString).mkString.toCharArray)
      val element = new SphinxElement
      element.setURL(new File(config[String]("limb_local_path")).toURI.relativize((new File(file.toString)).toURI).getPath)
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
      val doc = new Document(id)
      doc.addProperty(HEADER, element.getHeader)
      doc.addProperty(CONTENT, element.getContent)
      doc.addProperty(URL, element.getURL)
      val prepareQuery = "INSERT INTO `id_url` (id, url, header, content) VALUES (?, ?, ?, ?);"
      val query = connection.prepareStatement(prepareQuery)
      query.setInt(1, id)
      query.setString(2, element.getURL)
      query.setString(3, element.getHeader)
      query.setString(4, element.getContent)
      query.executeUpdate
      index.addDocuments(doc)
      id += 1
    }
    val query = connection.prepareStatement("INSERT INTO updates (start_id, stop_id) VALUES (?, ?)")
    query.setInt(1, startId)
    query.setInt(2, id)
    query.executeUpdate
    index.close()
  }
}
