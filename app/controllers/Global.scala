import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future
import java.io.File
import com.typesafe.config.ConfigFactory

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

  override def onError(request: RequestHeader, ex: Throwable) = {
    Future.successful(InternalServerError("Internal error"))
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound("Not found"))
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    Future.successful(BadRequest("Bad Request"))
  }

  override def onLoadConfig(config: Configuration, path: File, classloader: ClassLoader, mode: Mode.Mode): Configuration = {
    val modeSpecificConfig = config ++ Configuration(ConfigFactory.load("application.development.conf"))
    Logger.debug("config loaded")
    super.onLoadConfig(modeSpecificConfig, path, classloader, mode)

  }

}
