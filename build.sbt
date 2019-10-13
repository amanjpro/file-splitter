val Organization = "me.amanj"
val ProjectName = "file-splitter"
val ProjectScalaVersion = "2.13.0"
val LibraryDependencies = Seq(
  "software.amazon.awssdk" % "s3" % "2.9.13",
  "org.apache.hadoop" % "hadoop-client" % "3.2.1",
  "com.github.scopt" %% "scopt" % "3.7.1",
  "com.hierynomus" % "sshj" % "0.27.0" excludeAll {
     ExclusionRule(organization = "org.bouncycastle")
  },
  "org.scalatest" %% "scalatest" % "3.0.8" % "test"
)

organization in ThisBuild := Organization
name := ProjectName
scalaVersion in ThisBuild := ProjectScalaVersion
crossPaths in ThisBuild := false
publishMavenStyle in ThisBuild := true
version in ThisBuild := "0.3.0-SNAPSHOT"
coverageEnabled in ThisBuild := true

def project(baseDir: String, plugin: Option[AutoPlugin] = None): Project = {
  val projectId = s"$ProjectName-$baseDir"

  val prj = Project(id = projectId, base = file(baseDir))
    .settings(Seq(name := projectId,
    exportJars in Compile := false,
    ))
  plugin.map {
    case p@DistributionPlugin =>
      prj.enablePlugins(p).settings(projectName := ProjectName)
    case p@AssemblerPlugin    =>
      prj.enablePlugins(p).settings(Seq(publishMavenStyle := true,
        distributedProjectName := ProjectName))
  }.getOrElse(prj.settings(packagedArtifacts := Map.empty))
}


lazy val app = project("app", Some(AssemblerPlugin))
  .settings(libraryDependencies ++= LibraryDependencies)

lazy val distribution = project("distribution", Some(DistributionPlugin)).settings(
  (packageBin in Compile) := ((packageBin in Compile) dependsOn (
    packageBin in Compile in app)).value
)

