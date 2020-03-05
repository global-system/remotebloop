import NativePackagerHelper._
name := "RemoteBloop"
organization in ThisBuild := "ru.bitec"
scalaVersion in ThisBuild := "2.12.8"
version := "0.1.0"

lazy val global = project
  .in(file("."))
  .settings(settings)
  .aggregate(
    lbpserver,
    lbpcommander
  )

lazy val lbpserver = project
  .settings(
    name := "lbpserver",
    settings,
    libraryDependencies ++= commonDependencies,
    libraryDependencies ++= Seq(
      "org.glassfish.jersey.core" % "jersey-server" % "2.30.1",
      "org.glassfish.jersey.containers" % "jersey-container-grizzly2-http" % "2.30.1",
      "org.glassfish.jersey.inject" % "jersey-hk2" % "2.30.1",
      "org.glassfish.jersey.media" % "jersey-media-sse" % "2.30.1"
      //"com.sun.jersey" % "jersey-core" % "2.30.1",
      //"com.sun.jersey" % "jersey-server" % "2.30.1",
      //"jakarta.ws.rs" % "jakarta.ws.rs-api" % "2.1.6"
    ),
   mappings in Universal += {
     val jar = (packageBin in Compile).value
     jar -> ("lib/" + jar.getName)
   },
    mappings in Universal ++= {
      val files = (externalDependencyClasspath in Runtime).value
      files.map{f => f.data -> ("lib\\"+f.data.name)}
    },
    mappings in Universal ++= directory("lbpserver\\bin"),
    mappings in Universal ++= directory("lbpserver\\config")
  ).enablePlugins(UniversalPlugin)

lazy val lbpcommander = project
  .settings(
    name := "lbpcommander",
    settings,
    libraryDependencies ++= commonDependencies
  )
lazy val commonDependencies = Seq(
  "org.apache.commons" % "commons-text" % "1.8",
  "com.beust" % "jcommander" % "1.78",
  "org.apache.httpcomponents" % "httpclient" % "4.5.11",
  "commons-io" % "commons-io" % "2.6"
)

lazy val settings = commonSettings  
lazy val commonSettings = Seq(
)