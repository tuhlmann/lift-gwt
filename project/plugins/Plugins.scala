import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {

  val scalatoolsSnapshot = "Scala Tools Snapshot" at "http://scala-tools.org/repo-snapshots/"
  val sbtIdeaRepo = "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

  //val lessRepo = "lessis repo" at "http://repo.lessis.me"
  //val growl = "me.lessis" % "sbt-growl-plugin" % "0.0.5"

//  val repo = "GH-pages repo" at "http://mpeltonen.github.com/maven/"
  val idea = "com.github.mpeltonen" % "sbt-idea-plugin" % "0.1-SNAPSHOT"

  //lazy val eclipse = "de.element34" % "sbt-eclipsify" % "0.6.0"

//  lazy val codefellow = "de.tuxed" % "codefellow-plugin" % "0.3"

//  val formatter = "com.github.olim7t" % "sbt-scalariform" % "1.0.1"
  
}
