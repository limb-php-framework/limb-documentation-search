import AssemblyKeys._

assemblySettings

test in assembly := {}

jarName in assembly := "searcher.jar"

mergeStrategy in assembly <<= (mergeStrategy in assembly) { mergeStrategy =>
  {
    case entry => {
      val strategy = mergeStrategy(entry)
      entry match {
        case "public/javascripts/jquery-1.7.1.js" => MergeStrategy.discard
        case "public/javascripts/react.js" => MergeStrategy.discard
        case "public/images/play20header.png" => MergeStrategy.discard
        case "public/javascripts" => MergeStrategy.discard
        case "public/javascripts/require.js" => MergeStrategy.discard
        case "public" => MergeStrategy.discard
        case "public/javascripts/jquery-play-1.7.1.js" => MergeStrategy.discard
        case "public/stylesheets/bootstrap.css" => MergeStrategy.discard
        case "public/stylesheets" => MergeStrategy.discard
        case "public/stylesheets/main.css" => MergeStrategy.discard
        case "public/javascripts/jquery-play-1.7.1.js.gz" => MergeStrategy.discard
        case "public/images" => MergeStrategy.discard
        case "public/javascripts/main.js" => MergeStrategy.discard
        case "public/javascripts/jquery-1.7.1.js.gz" => MergeStrategy.discard
        case "public/images/favicon.png" => MergeStrategy.discard
        case "public/javascripts/bootstrap.js" => MergeStrategy.discard
        case "public/javascripts/jquery-1.9.0.min.js" => MergeStrategy.discard
        case "public/images/favicon.png" => MergeStrategy.discard
        case "public/images/play20header.png" => MergeStrategy.discard
        case "public/javascripts/bootstrap.js" => MergeStrategy.discard
        case "public/javascripts/jquery-1.7.1.js" => MergeStrategy.discard
        case "public/javascripts/jquery-1.7.1.js.gz" => MergeStrategy.discard
        case "public/javascripts/jquery-1.9.0.min.js" => MergeStrategy.discard
        case "public/javascripts/jquery-play-1.7.1.js" => MergeStrategy.discard
        case "public/javascripts/jquery-play-1.7.1.js.gz" => MergeStrategy.discard
        case "public/javascripts/main.js" => MergeStrategy.discard
        case "public/javascripts/react.js" => MergeStrategy.discard
        case "public/javascripts/require.js" => MergeStrategy.discard
        case "public/stylesheets/bootstrap.css" => MergeStrategy.discard
        case "public/stylesheets/main.css" => MergeStrategy.discard
        case _ => if (strategy == MergeStrategy.deduplicate) {
          MergeStrategy.first
        } else {
          strategy
        }
      }
    }
  }
}
