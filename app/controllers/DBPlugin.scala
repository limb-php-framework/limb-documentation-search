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


class BoneCPPluginCustom(app: Application) extends BoneCPPlugin(app) {

  private def error = throw new Exception("db keys are missing from application.conf")

  private def dbURL(conn: Connection): String = {
    val u = conn.getMetaData.getURL
    conn.close()
    u
  }

  private lazy val dbApi: DBApi = new BoneCPApi(dbConfig, app.classloader)

  private lazy val isDisabled = {
    app.configuration.getString("dbplugincustom").filter(_ == "disabled").isDefined || dbConfig.subKeys.isEmpty
  }

  override def enabled = isDisabled == false

  override def onStart() {
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

}
