import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future
import java.io.File
import com.typesafe.config.ConfigFactory
import com.sun.jna.{Library, Native, Platform}
import sys.process._
import play.Configuration.root


object Global extends GlobalSettings {

  trait CLibrary extends Library {
    def setuid(id: Int): Int
    def getuid(): Int
  }

  def getIdFromUserName = ("id -u " + root.getString("from_user")).!!.split("\n")(0).toInt

  def CLibraryInstance = Native.loadLibrary("/lib/x86_64-linux-gnu/libc.so.6",  classOf[CLibrary]).asInstanceOf[CLibrary]

  override def onStart(app: Application) {
    CLibraryInstance.setuid(getIdFromUserName)
    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

  override def onError(request: RequestHeader, ex: Throwable) = {
    Future.successful(InternalServerError(views.html.internalError()))
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound(views.html.notFound()))
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful(BadRequest("Bad Request"))
  }

  override def onLoadConfig(config: Configuration, path: File, classloader: ClassLoader, mode: Mode.Mode): Configuration = {
    val modeSpecificConfig = config ++ Configuration(ConfigFactory.load("application.dev.conf"))
    Logger.debug("config loaded")
    super.onLoadConfig(modeSpecificConfig, path, classloader, mode)

  }

}
