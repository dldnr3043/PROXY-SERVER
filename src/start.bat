@echo off

set JAVA_PATH=D:\PALETTE\hkcloud\rnd\java\java-1.8.0-openjdk-1.8.0.252-2.b09\bin

%JAVA_PATH%\java -classpath D:\PROXY ProxyServer 8080 127.0.0.1 8443

pause