name := "sbtMonkeyProject"

version := "0.3"

scalaVersion := "2.13.5"

scalacOptions ++= Seq(
  "-encoding", "utf8", // Option and arguments on same  line
  "-Xfatal-warnings", // New lines for each options
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)


//JME
resolvers += Resolver.jcenterRepo
resolvers += Resolver.bintrayRepo("jmonkeyengine", "org.jmonkeyengine")


//val recastVer = "1.3.2"
val recastLibs = Seq(
  "org.recast4j" % "recast" % "1.3.2",
  "org.recast4j" % "detour" % "1.3.2",
  "org.recast4j" % "detour-crowd" % "1.3.2",
  "org.recast4j" % "detour-tile-cache" % "1.3.2",
  "org.recast4j" % "detour-extras" % "1.3.2",
  "org.recast4j" % "detour-dynamic" % "1.3.2",
)

val jmeVer = "3.3.2-stable"
val jmeLibs = Seq(
  "org.jmonkeyengine" % "jme3-core" % jmeVer,
  "org.jmonkeyengine" % "jme3-desktop" % jmeVer,

  //  "org.jmonkeyengine" % "jme3-lwjgl" % jmeVer, // Desktop renderer for jME3
  //  "org.jmonkeyengine" % "jme3-jogl" % jmeVer, //JOGL based renderer (optional replacement for lwjgl / lwjgl3)
  "org.jmonkeyengine" % "jme3-lwjgl3" % jmeVer, //NEW since jME3.1! LWJGL3-based desktop renderer for jME3 (beta)

  "org.jmonkeyengine" % "jme3-testdata" % jmeVer,

  "org.jmonkeyengine" % "jme3-effects" % jmeVer, //Effects libraries for water and other post filters
  "org.jmonkeyengine" % "jme3-plugins" % jmeVer, //Loader plugins for OgreXML and jME-XML
  "org.jmonkeyengine" % "jme3-terrain" % jmeVer,
  "org.jmonkeyengine" % "jme3-jogg" % jmeVer,

  "org.jmonkeyengine" % "jme3-bullet-native" % jmeVer,
  "org.jmonkeyengine" % "jme3-bullet" % jmeVer,

  "org.jmonkeyengine" % "jme3-niftygui" % jmeVer,
)

libraryDependencies ++= jmeLibs
libraryDependencies ++= recastLibs
libraryDependencies += "org.slf4j" % "jul-to-slf4j" % "1.7.22"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"


//useCoursier := false
//retrieveManaged := true