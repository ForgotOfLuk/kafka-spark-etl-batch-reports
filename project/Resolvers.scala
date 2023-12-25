import sbt.*

object Resolvers {
  val commonResolvers: Seq[Resolver] = Seq(
    Classpaths.typesafeReleases,
    "confluent" at "https://packages.confluent.io/maven/",
  )
}
