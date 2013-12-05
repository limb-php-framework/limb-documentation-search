package indexer

import java.sql.DriverManager
import org.streum.configrity._
import com.sphinxsearch.indexer.{Document, Index, IndexDescription}
import org.pegdown.{PegDownProcessor, Extensions}
import org.pegdown.ast.{TextNode, RootNode, HeaderNode, Node}
import org.apache.commons.io.FileUtils
import java.io.File
import scala.collection.JavaConversions._
import scala.io.Source
import scala.collection.mutable.{ArrayBuffer, Buffer}
import sys.process._
import java.io.File

object xmlpipe2Generator {

  val config = Configuration.load("config")

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

  private def getTextFromHeaderNode(node: Node): String = {
    node.getChildren.map { subNode =>
      if (subNode.getClass == classOf[TextNode]) {
        omgGetTextFromTextNode(subNode)
      }
    } filter { _.getClass == classOf[String] } mkString(" ")
  }

  private def omgGetTextFromTextNode(node: Node): String = {
    node.asInstanceOf[TextNode].getText
  }

  def main(args: Array[String]): Unit = {
    if (config[Boolean]("download_limb_if_no_exists")) {
      if (!(new File(config[String]("limb_local_path"))).exists) {
        val status = ("git clone " + config[String]("limb_git_path") + " " + config[String]("limb_local_path")) #> (new File("/dev/null")) !

        if (status != 0 ) {
          System.exit(status)
        }
      }
    }
    Class.forName("org.sqlite.JDBC")
    val db_file = new File(config[String]("db_path"))
    if (db_file.exists) {
      db_file.delete
    }
    val connection = DriverManager.getConnection("jdbc:sqlite:" + config[String]("db_path"))
    val statement = connection.createStatement()
    statement.executeUpdate("CREATE TABLE id_url (id INTEGER, url TEXT, header TEXT, content TEXT)")
    val processor = new PegDownProcessor(Extensions.ALL)
    val mdFiles = FileUtils.iterateFiles(new File(config[String]("limb_local_path")), Array("md"), true)
    val HEADER = "header"
    val CONTENT = "content"
    val URL = "url"
    val CODE = "code"
    val desc = IndexDescription.createIndexDescription(HEADER, CONTENT)
    desc.addStr2OrdinalAttribute(URL)
    val index = Index.createIndex(desc)
    var id = 1
    for (file <- mdFiles) {
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
    index.close()
  }
}
