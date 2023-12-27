import sbt.ThisBuild
import sbtassembly.AssemblyKeys.assemblyMergeStrategy
import sbtassembly.{MergeStrategy, PathList}

object MergeStrategyBuilder {

  val mergeStrategy: String => MergeStrategy = {
    case PathList("META-INF", xs*) if xs.nonEmpty && xs.last == "MANIFEST.MF" => MergeStrategy.discard
    case PathList("META-INF", "io.netty.versions.properties", _*) => MergeStrategy.first
    case "module-info.class" => MergeStrategy.discard
    case PathList("META-INF", "versions", "9", "module-info.class", _*) => MergeStrategy.discard
    case "kafka/kafka-version.properties" => MergeStrategy.first
    case x => MergeStrategy.defaultMergeStrategy(x)
  }

  // Define the merge strategy function with explicit parameter type
  val sparkDailyMergeStrategy: String => MergeStrategy = {
    case PathList("META-INF", "native-image", "org.mongodb", "bson", "native-image.properties") => MergeStrategy.first
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case _ => MergeStrategy.first
  }

  def sparkMergeStrategy = {
    ThisBuild / assemblyMergeStrategy := {
      case PathList("META-INF", xs*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  }
}