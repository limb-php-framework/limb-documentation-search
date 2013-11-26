package indexer

import org.streum.configrity._
import com.sphinxsearch.indexer.{Document, Index, IndexDescription}
import org.pegdown.{PegDownProcessor, Extensions}
import org.pegdown.ast.{AbstractNode, VerbatimNode, TextNode, RootNode, HeaderNode, Node}
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

    def getHeader: String = {
      header
    }

    def getContent: String = {
      content
    }

    def addHeader(value: String) = {
      header += value
    }

    def addContent(value: String) = {
      content += " " + value
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

    val processor = new PegDownProcessor(Extensions.ALL)
    val mdFiles = FileUtils.iterateFiles(new File(config[String]("limb_local_path")), Array("md"), true)
    val HEADER = "header"
    val CONTENT = "content"
    val URL = "url"
    val desc = IndexDescription.createIndexDescription(HEADER, CONTENT, URL)
    val index = Index.createIndex(desc)
    var i = 1
    for (file <- mdFiles) {
      val mdTree = processor.parseMarkdown(Source.fromFile(file.toString).mkString.toCharArray)
      val element = new SphinxElement
      def getTextFromOtherNodes(nodeList: java.util.List[Node]): Unit = {
        nodeList.foreach { node =>
          node match {
            case _: TextNode => element.addContent(omgGetTextFromTextNode(node))
            case _: HeaderNode => element.addHeader(getTextFromHeaderNode(node))
            case _ => getTextFromOtherNodes(node.getChildren)
          }
        }
      }
      getTextFromOtherNodes(mdTree.getChildren)
      val doc = new Document(i)
      doc.addProperty(HEADER, element.getHeader)
      doc.addProperty(CONTENT, element.getContent)
      doc.addProperty(URL, file.toString)
      index.addDocuments(doc)
      i += 1
    }
    index.close()
  }
}
