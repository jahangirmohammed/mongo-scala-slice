import sbt.Keys._
import sbt._

name := "mongo-scala-slice"

resolvers += Resolver.sonatypeRepo("public")
scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.mongodb.scala" %% "mongo-scala-driver" % "1.1.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xfatal-warnings")
