#!/usr/bin/env bash
# Ending with dummy so we don't get any problems with windows line endings...
CLASSPATH=$CLASSPATH:/opt/data/lakemedelsnara/properties:$CATALINA_HOME/log4j2/lib/*:$CATALINA_HOME/log4j2/conf:/dummy
JAVA_OPTS="$JAVA_OPTS -Dlog4j2.formatMsgNoLookups=true"
