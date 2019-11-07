val Organization = "me.amanj"
val ProjectName = "file-splitter"
val ProjectScalaVersion = "2.13.0"
val LibraryDependencies = Seq(
  "software.amazon.awssdk" % "s3" % "2.10.1" excludeAll (
    ExclusionRule(organization = "com.fasterxml.jackson.core"),
    ExclusionRule(organization = "com.typesafe.akka",
      name = "akka-actor")
  ),
  "org.apache.hadoop" % "hadoop-hdfs-client" % "3.2.1" excludeAll (
    ExclusionRule(organization = "com.fasterxml.jackson.core"),
    ExclusionRule(organization = "org.eclipse.jetty"),
    ExclusionRule(organization = "org.apache.zookeeper",
      name = "zookeeper"),
    ExclusionRule(organization = "com.google.protobuf",
      name = "protobuf-java" ),
    ExclusionRule(organization = "commons-beanutils",
      name = "commons-beanutils" ),
    ExclusionRule(organization = "org.apache.commons",
      name = "commons-compress" ),
    ExclusionRule(organization = "com.beust", name = "jcommander")
  ),
  "org.apache.hadoop" % "hadoop-common" % "3.2.1" excludeAll (
    ExclusionRule(organization = "com.fasterxml.jackson.core"),
    ExclusionRule(organization = "org.eclipse.jetty"),
    ExclusionRule(organization = "org.apache.zookeeper",
      name = "zookeeper"),
    ExclusionRule(organization = "com.google.protobuf",
      name = "protobuf-java" ),
    ExclusionRule(organization = "commons-beanutils",
      name = "commons-beanutils" ),
    ExclusionRule(organization = "org.apache.commons",
      name = "commons-compress" ),
    ExclusionRule(organization = "com.beust", name = "jcommander")
  ),
  "com.google.protobuf" % "protobuf-java" % "3.10.0",
  "commons-beanutils" % "commons-beanutils" % "1.9.4",
  "org.apache.commons" % "commons-compress"  % "1.19",
  "com.beust" % "jcommander" % "1.78",
  "com.typesafe.akka" %% "akka-actor" % "2.5.26",
  "org.eclipse.jetty" % "jetty-util" % "9.4.22.v20191022",
  "org.eclipse.jetty" % "jetty-server" % "9.4.22.v20191022",
  "org.eclipse.jetty" % "jetty-servlet" % "9.4.22.v20191022",
  "org.eclipse.jetty" % "jetty-webapp" % "9.4.22.v20191022",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.10.0" ,
  "org.apache.zookeeper" % "zookeeper" % "3.5.6",
  "com.github.scopt" %% "scopt" % "3.7.1",
  "com.hierynomus" % "sshj" % "0.27.0",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test,it",
  "software.amazon.awssdk" % "url-connection-client" % "2.10.1" % "it" excludeAll (
    ExclusionRule(organization = "com.fasterxml.jackson.core"),
    ExclusionRule(organization = "com.typesafe.akka",
      name = "akka-actor")
  )
)

organization in ThisBuild := Organization
name := ProjectName
scalaVersion in ThisBuild := ProjectScalaVersion
crossPaths in ThisBuild := false
publishMavenStyle in ThisBuild := true
version in ThisBuild := "0.6.0-SNAPSHOT"
coverageEnabled in ThisBuild := true

scalacOptions in ThisBuild ++= Seq(
  "-encoding", "UTF-8", "-unchecked", "-deprecation",
  "-feature", "-Xlint", "-Xfatal-warnings")

def project(baseDir: String, plugin: Option[AutoPlugin] = None): Project = {
  val projectId = s"$ProjectName-$baseDir"

  val prj = Project(id = projectId, base = file(baseDir))
    .settings( Seq(
        name := projectId,
        exportJars in Compile := false,
        assemblyMergeStrategy in assembly := {
          case PathList("META-INF", xs@_*) =>
            xs.map(_.toLowerCase) match {
              case ps @ (x :: xs) if ps.last.endsWith(".sf")
                || ps.last.endsWith(".dsa") || ps.last.endsWith(".rsa") =>
                MergeStrategy.discard
              case ("manifest.mf" :: Nil) |
                   ("index.list" :: Nil) |
                   ("dependencies" :: Nil) |
                   ("license" :: Nil) |
                   ("notice" :: Nil) => MergeStrategy.discard
              case _ => MergeStrategy.first // was 'discard' previousely
            }
          case "reference.conf" => MergeStrategy.concat
          case _ => MergeStrategy.first
        }
      )
    )
  plugin.map {
    case p@DistributionPlugin =>
      prj.enablePlugins(p).settings(projectName := ProjectName)
    case p@AssemblerPlugin    =>
      prj.enablePlugins(p).settings(Seq(publishMavenStyle := true,
        distributedProjectName := ProjectName))
  }.getOrElse(prj.settings(packagedArtifacts := Map.empty))
}


lazy val app = project("app", Some(AssemblerPlugin))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings ++
    Seq(
      parallelExecution in IntegrationTest := false,
      libraryDependencies ++= LibraryDependencies))

lazy val distribution = project("distribution", Some(DistributionPlugin)).settings(
  (packageBin in Compile) := ((packageBin in Compile) dependsOn (
    packageBin in Compile in app)).value
)

