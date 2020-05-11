FROM tomcat:9-jdk11-openjdk
COPY core-bc/modules/web/target/bestallning10.war /usr/local/tomcat/webapps/
COPY dockerfiles/setclasspath.sh /usr/local/tomcat/bin/
RUN chmod +x /usr/local/tomcat/bin/setclasspath.sh
