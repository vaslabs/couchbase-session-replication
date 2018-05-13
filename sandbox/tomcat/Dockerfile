FROM tomcat:7.0.86-jre8-alpine

COPY conf/context.xml /usr/local/tomcat/conf/context.xml
COPY webapps/sample.war /usr/local/tomcat/webapps/sample.war
COPY ./entrypoint.sh /entrypoint.sh

RUN chmod +x /entrypoint.sh

VOLUME /tomcat-lib

ENTRYPOINT /entrypoint.sh