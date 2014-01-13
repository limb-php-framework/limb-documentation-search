package controllers

import play.Configuration.root

object RemoteAssets {

  def url(path: String): String = {
    Seq(root.getString("static_url"), path).mkString("/")
  }

}
