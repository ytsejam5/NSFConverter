@echo off
rem Licensed to the Apache Software Foundation (ASF) under one or more
rem contributor license agreements.  See the NOTICE file distributed with
rem this work for additional information regarding copyright ownership.
rem The ASF licenses this file to You under the Apache License, Version 2.0
rem (the "License"); you may not use this file except in compliance with
rem the License.  You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.

rem ---------------------------------------------------------------------------
rem Set JAVA_HOME or JRE_HOME if not already set, ensure any provided settings
rem are valid and consistent with the selected start-up options and set up the
rem endorsed directory.
rem ---------------------------------------------------------------------------

rem Make sure prerequisite environment variables are set

rem set PATH=%PATH%;C:\opt\IBM\Notes901
rem set LD_LIBRARY_PATH=C:\opt\IBM\Notes901
set JAVA_OPTS=-Djava.library.path=C:\opt\IBM\Notes901 -Xj9 -Xmx1024m

set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\deploy.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\ibmallorb.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\ibmjcefw.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\ibmpkcs.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\j9zip.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\javaws.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\plugin.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\rt.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\tools.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\vm.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\xmldsigfw.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\ext\healthcenter.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\ext\ibmallext.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\ext\ibmcac.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\ext\ibmjcefips.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\ext\ibmjceprovider.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\ext\ibmpkcs11impl.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\ext\IBMSecureRandom.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\ext\JavaDiagnosticsCollector.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\ext\localedata.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\ext\njempcl.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\ext\Notes.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\ext\websvc.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\im\indicim.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\im\thaiim.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\security\local_policy.jar
set CLASSPATH=%CLASSPATH%;c:\opt\IBM\Notes901\jvm\lib\security\US_export_policy.jar


rem In debug mode we need a real JDK (JAVA_HOME)
if ""%1"" == ""debug"" goto needJavaHome

rem Otherwise either JRE or JDK are fine
if not "%JRE_HOME%" == "" goto gotJreHome
if not "%JAVA_HOME%" == "" goto gotJavaHome
echo Neither the JAVA_HOME nor the JRE_HOME environment variable is defined
echo At least one of these environment variable is needed to run this program
goto exit

:needJavaHome
rem Check if we have a usable JDK
if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if not exist "%JAVA_HOME%\bin\javaw.exe" goto noJavaHome
if not exist "%JAVA_HOME%\bin\jdb.exe" goto noJavaHome
if not exist "%JAVA_HOME%\bin\javac.exe" goto noJavaHome
set "JRE_HOME=%JAVA_HOME%"
goto okJava

:noJavaHome
echo The JAVA_HOME environment variable is not defined correctly.
echo It is needed to run this program in debug mode.
echo NB: JAVA_HOME should point to a JDK not a JRE.
goto exit

:gotJavaHome
rem No JRE given, use JAVA_HOME as JRE_HOME
set "JRE_HOME=%JAVA_HOME%"

:gotJreHome
rem Check if we have a usable JRE
if not exist "%JRE_HOME%\bin\java.exe" goto noJreHome
if not exist "%JRE_HOME%\bin\javaw.exe" goto noJreHome
goto okJava

:noJreHome
rem Needed at least a JRE
echo The JRE_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto exit

:okJava
rem Don't override the endorsed dir if the user has set it previously
if not "%JAVA_ENDORSED_DIRS%" == "" goto gotEndorseddir
rem Set the default -Djava.endorsed.dirs argument
set "JAVA_ENDORSED_DIRS=%CATALINA_HOME%\endorsed"
:gotEndorseddir

rem Don't override _RUNJAVA if the user has set it previously
if not "%_RUNJAVA%" == "" goto gotRunJava
rem Set standard command for invoking Java.
rem Also note the quoting as JRE_HOME may contain spaces.
set _RUNJAVA="%JRE_HOME%\bin\java.exe"
:gotRunJava

rem Don't override _RUNJDB if the user has set it previously
rem Also note the quoting as JAVA_HOME may contain spaces.
if not "%_RUNJDB%" == "" goto gotRunJdb
set _RUNJDB="%JAVA_HOME%\bin\jdb.exe"
:gotRunJdb

goto end

:exit
exit /b 1

:end
exit /b 0
