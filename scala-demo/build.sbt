name := "scala-demo"

version := "0.1"

resolvers += Resolver.mavenLocal

scalaVersion := "2.12.5"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.6"
libraryDependencies +=  "com.github.wangzaixiang" %% "scala-sql" % "2.0.6"
libraryDependencies += "junit" % "junit" % "4.12" % Test