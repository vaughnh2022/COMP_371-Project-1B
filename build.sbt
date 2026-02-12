name := "topwords"

version := "0.4"

libraryDependencies ++= Seq(
  "com.github.sbt.junit" % "jupiter-interface" % "0.17.0" % Test, // required only for plain JUnit testing
  "org.scalatest"     %% "scalatest"       % "3.2.19"   % Test,
  "org.scalacheck"    %% "scalacheck"      % "1.19.0"   % Test,
  "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0" % Test
)

enablePlugins(JavaAppPackaging)

Compile / mainClass := Some("topwords.Main")

assembly / assemblyOutputPath := file("topwords.jar")
