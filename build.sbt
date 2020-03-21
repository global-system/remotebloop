import NativePackagerHelper._
name := "RemoteBloop"
organization in ThisBuild := "ru.bitec"
scalaVersion in ThisBuild := "2.12.8"
version := "0.1.0"

lazy val global = project
  .in(file("."))
  .settings(settings,
    mappings in Universal ++= directory("bin"),
    mappings in Universal ++= {
      directory("config.origin").map{case (file,_)=> file -> ("config/" + file.name)}
    },
    mappings in Universal += {
      val jar = (packageBin in Compile in rbpserver).value
      jar -> ("lib/rbpserver/" + jar.getName)
    },
    mappings in Universal ++= {
      val files = (externalDependencyClasspath in Runtime in rbpserver).value
      files.map{f => f.data -> ("lib/rbpserver/"+f.data.name)}
    }/*,
    mappings in Universal ++= {
      val files = (externalDependencyClasspath in Runtime in rbplauncher).value
      files.map{f => f.data -> ("lib\\rbplauncher\\"+f.data.name)}
    }*/
  ).aggregate(
    rbpserver,
    rbpcommander
  ).enablePlugins(UniversalPlugin)

lazy val rbpserver = project
  .settings(
    name := "rbpserver",
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
    )
  )

lazy val rbpcommander = project
  .settings(
    name := "rbpcommander",
    settings,
    libraryDependencies ++= commonDependencies,
    libraryDependencies ++= Seq(
      "org.scala-sbt" %% "zinc" % "1.3.0-M4",
      "ch.epfl.scala" % "bsp4j" % "2.0.0-M6",
      "ch.epfl.scala" %% "bloop-config" % "1.4.0-RC1",
      "io.monix" %% "monix" % "3.1.0"
    ),
    testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-l","ru.bitec.remotebloop.rbpcommander.ManualTag")
  )
lazy val rbplauncher = project
  .settings(
    name := "rbplauncher",
    settings,
    libraryDependencies ++= Seq(
      "ch.epfl.scala" %% "bloop-launcher" % "1.4.0-RC1"
    )
  )
lazy val commonDependencies = Seq(
  "org.apache.commons" % "commons-text" % "1.8",
  "com.beust" % "jcommander" % "1.78",
  "org.apache.httpcomponents" % "httpclient" % "4.5.11",
  "commons-io" % "commons-io" % "2.6",
  "org.scala-sbt" %% "io" % "1.3.3",
  "org.scalatest" %% "scalatest" % "3.1.1" % "test",
  "com.typesafe.play" %% "play-json" % "2.8.1"
)

lazy val settings = commonSettings  
lazy val commonSettings = Seq(
)