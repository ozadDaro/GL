#!/bin/bash
echo	"Enabiling JDK 8"
export JAVA_HOME=/etc/alternatives/java_sdk_1.8.0
export PATH=$JAVA_HOME/bin:$PATH
java -version
javac -version
