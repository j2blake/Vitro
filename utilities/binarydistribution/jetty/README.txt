This directory contains the Jetty components that we need:

-------------------------------
jetty-runner-XXXXXX.jar

This is the main Jetty executable, packaged as a JAR file. It is not part of 
the standard Jetty distribution. 

-------------------------------
start.jar

In the Windows scripts (startup.bat and shutdown.bat), this is used to stop a
running instance of Jetty. It is also used as a "poor man's Telnet", to 
ascertain whether Jetty is running.

This is part of the standard Jetty distribution.

-------------------------------
jsp/

This directory of JAR files is the Eclipse Java Compiler. Since we are targeting
machines with JREs (but no JDKs), Jetty will be configured to use this compiler
to process JSPs.

-------------------------------

Get Jetty-Runner here:
   http://central.maven.org/maven2/org/eclipse/jetty/jetty-runner/

Get start.jar and jsp/ here:
   http://download.eclipse.org/jetty/
   
(The release number of the Jetty distribution should match the release number
of the Jetty-Runner JAR.)

Documentation for Jetty-Runner and start.jar is here:
   http://www.eclipse.org/jetty/documentation/9.2.8.v20150217/runner.html

Documentation for jsp/ is here:
   https://wiki.eclipse.org/Jetty/Howto/Configure_JSP, in the section entitled "Compiling JSPs"