package play.api.db

import scala.language.reflectiveCalls
import play.api._
import play.api.libs._
import play.core._
import java.sql._
import javax.sql._
import com.jolbox.bonecp._
import com.jolbox.bonecp.hooks._
import scala.util.control.{ NonFatal, ControlThrowable }

//

class BoneCPPlugin(app: Application) extends DBPlugin {

  private def error = throw new Exception("db keys are missing from application.conf")

  lazy val dbConfig = app.configuration.getConfig("db").getOrElse(Configuration.empty)

  private def dbURL(conn: Connection): String = {
    val u = conn.getMetaData.getURL
    conn.close()
    u
  }

  // should be accessed in onStart first
  private lazy val dbApi: DBApi = new BoneCPApi(dbConfig, app.classloader)

  /**
   * plugin is disabled if either configuration is missing or the plugin is explicitly disabled
   */
  private lazy val isDisabled = {
    app.configuration.getString("dbplugin").filter(_ == "disabled").isDefined || dbConfig.subKeys.isEmpty
  }

  /**
   * Is this plugin enabled.
   *
   * {{{
   * dbplugin=disabled
   * }}}
   */
  override def enabled = isDisabled == false

  /**
   * Retrieves the underlying `DBApi` managing the data sources.
   */
  def api: DBApi = dbApi

  /**
   * Reads the configuration and connects to every data source.
   */
  override def onStart() {
    // Try to connect to each, this should be the first access to dbApi
    dbApi.datasources.map { ds =>
      try {
        ds._1.getConnection.close()
        app.mode match {
          case Mode.Test =>
          case mode => Play.logger.info("database [" + ds._2 + "] connected at " + dbURL(ds._1.getConnection))
        }
      } catch {
        case NonFatal(e) => null
      }
    }
  }

  /**
   * Closes all data sources.
   */
  override def onStop() {
    dbApi.datasources.foreach {
      case (ds, _) => try {
        dbApi.shutdownPool(ds)
      } catch { case NonFatal(_) => }
    }
    val drivers = DriverManager.getDrivers()
    while (drivers.hasMoreElements) {
      val driver = drivers.nextElement
      DriverManager.deregisterDriver(driver)
    }
  }

}
