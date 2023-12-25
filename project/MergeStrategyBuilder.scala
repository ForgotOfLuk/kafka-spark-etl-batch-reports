import sbt.ThisBuild
import sbtassembly.AssemblyKeys.assemblyMergeStrategy
import sbtassembly.AssemblyPlugin.autoImport.MergeStrategy
import sbtassembly.PathList

object MergeStrategyBuilder {
  def mergeStrategy = {
    ThisBuild / assemblyMergeStrategy := {
      case PathList("META-INF", xs*) if xs.nonEmpty && xs.last == "MANIFEST.MF" => MergeStrategy.discard
      case PathList("META-INF", "io.netty.versions.properties", _*) => MergeStrategy.first
      case "module-info.class" => MergeStrategy.discard
      case PathList("META-INF", "versions", "9", "module-info.class", _*) => MergeStrategy.discard
      case "kafka/kafka-version.properties" => MergeStrategy.first
      case x => MergeStrategy.defaultMergeStrategy(x)
    }
  }
  def sparkMergeStrategy = {
    ThisBuild / assemblyMergeStrategy := {
      case PathList("META-INF", xs*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  }
}