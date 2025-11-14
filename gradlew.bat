@ECHO OFF
@REM -----------------------------------------------------------------------------
@REM Gradle startup script for Windows
@REM -----------------------------------------------------------------------------

SETLOCAL

SET APP_HOME=%~dp0
FOR %%i IN ("%APP_HOME:~0,-1%") DO SET APP_HOME=%%~fi

SET CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

IF NOT "%JAVA_HOME%"=="" (
    SET "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) ELSE (
    SET "JAVA_EXE=java.exe"
)

IF NOT EXIST "%JAVA_EXE%" (
    ECHO ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.>&2
    ECHO Please set the JAVA_HOME variable in your environment to match the&& ^
        location of your Java installation.>&2
    EXIT /B 1
)

SET JAVA_OPTS=%JAVA_OPTS%

"%JAVA_EXE%" %JAVA_OPTS% ^
  -classpath "%CLASSPATH%" ^
  org.gradle.wrapper.GradleWrapperMain %*

ENDLOCAL

