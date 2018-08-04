organization := "com.today"

name := "data-migration"

version := "1.1-SNAPSHOT"

scalaVersion := "2.12.2"

resolvers ++= List(
  "JBoss" at "https://repository.jboss.org",
  "today nexus" at "http://nexus.today36524.com/repository/maven-public/"
  )
publishTo := Some("today-snapshots" at "http://nexus.today36524.com/repository/maven-snapshots/")

credentials += Credentials("Sonatype Nexus Repository Manager", "nexus-inner.today.cn", "central-services", "E@Z.nrW3")

libraryDependencies ++= Seq(
  "com.github.dapeng" % "dapeng-spring" % "2.0.2",
  "com.github.wangzaixiang" %% "scala-sql" % "2.0.3",
  "org.slf4j" % "slf4j-api" % "1.7.13",
  "org.slf4j" % "jcl-over-slf4j" % "1.7.25",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "ch.qos.logback" % "logback-core" % "1.1.3",
  "mysql" % "mysql-connector-java" % "5.1.36",
  "com.github.noraui" % "ojdbc7" % "12.1.0.2",
  "com.alibaba" % "druid" % "1.0.17",
  "com.today" %% "idgen-api" % "2.0.2",
  "com.today" %% "member-api" % "0.3.3-SNAPSHOT",
  "com.today" %% "service-commons" % "1.4-SNAPSHOT",
  "org.springframework" % "spring-context" % "4.3.5.RELEASE",
  "org.springframework" % "spring-jdbc" % "4.3.5.RELEASE",
  "org.springframework" % "spring-test" % "4.3.5.RELEASE",
  "junit" % "junit" % "4.12",
  "redis.clients"% "jedis" %  "2.9.0",
  "org.springframework.data" % "spring-data-redis" % "1.8.4.RELEASE",
  "org.scalaj" % "scalaj-http_2.12" % "2.3.0"

)

javacOptions ++= Seq("-encoding", "UTF-8")


assemblyMergeStrategy in assembly := {
  case PathList("org", "slf4j", xs @ _*)         => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "spring.factories" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "changelog.txt" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "Log.class" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "LogConfigurationException.class" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "LogFactory.class" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "NoOpLog.class" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "SimpleLog$1.class" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "SimpleLog.class" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "common.thrift" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "commons-lang3.jar" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

mainClass in assembly := Some("com.today.main.MemberMain")

assemblyJarName in assembly := "migration.jar"

enablePlugins(PackPlugin)
packMain := Map("migration" -> "com.today.main.MemberMain")
