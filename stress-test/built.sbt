name := "couchbase-session-replication-stress-test"

version := "1.0"

scalaVersion := "2.12.4"

organization := "org.vaslabs"

enablePlugins(GatlingPlugin)

libraryDependencies ++= Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.3.0",
  "io.gatling"            % "gatling-test-framework"    % "2.3.0"
)

