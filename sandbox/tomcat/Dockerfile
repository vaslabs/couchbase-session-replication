FROM tomcat:7.0.86-jre8-alpine

COPY conf/context.xml /usr/local/tomcat/conf/context.xml
COPY webapps/sample.war /usr/local/tomcat/webapps/sample.war
COPY ./target/tomcat7-session-replication-1.0-SNAPSHOT.jar /usr/local/tomcat/lib/