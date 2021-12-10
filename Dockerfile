FROM tomcat:9-jdk11-openjdk-slim
COPY dockerfiles/setenv.sh /usr/local/tomcat/bin/
COPY dockerfiles/log4j2/ /usr/local/tomcat/log4j2/
RUN chmod +x /usr/local/tomcat/bin/setenv.sh
RUN sed 's/jdk.tls.disabledAlgorithms=SSLv3, TLSv1, TLSv1.1, RC4, DES, MD5withRSA/jdk.tls.disabledAlgorithms=SSLv3, TLSv1, RC4, DES, MD5withRSA/g' /usr/local/openjdk-11/conf/security/java.security > /usr/local/openjdk-11/conf/security/java.security.modified
RUN cp /usr/local/openjdk-11/conf/security/java.security.modified /usr/local/openjdk-11/conf/security/java.security
COPY core-bc/modules/web/target/bestallning10.war /usr/local/tomcat/webapps/
