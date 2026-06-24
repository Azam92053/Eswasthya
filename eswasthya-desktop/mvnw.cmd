@echo off
:: ----------------------------------------------------------------------------
:: Maven Start Up Batch script (Windows)
:: Licensed to the Apache Software Foundation (ASF)
:: ----------------------------------------------------------------------------

setlocal enabledelayedexpansion

:: Check Java
if not defined JAVA_HOME (
    where java >nul 2>&1
    if errorlevel 1 (
        echo Error: JAVA_HOME is not set and java is not found in PATH.
        echo Please install Java 17+ and set JAVA_HOME.
        exit /b 1
    )
    set JAVACMD=java
) else (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "JAVACMD=%JAVA_HOME%\bin\java.exe"
    ) else if exist "%JAVA_HOME%\java.exe" (
        set "JAVACMD=%JAVA_HOME%\java.exe"
        for %%I in ("%JAVA_HOME%\..") do set "JAVA_HOME=%%~fI"
    ) else (
        echo Error: JAVA_HOME is not defined correctly.
        echo JAVA_HOME should point to a JDK root folder or its bin folder.
        exit /b 1
    )
)

:: Paths
set SCRIPT_DIR=%~dp0
set WRAPPER_PROPS=%SCRIPT_DIR%.mvn\wrapper\maven-wrapper.properties

:: Read distributionUrl
for /f "tokens=2 delims==" %%A in ('findstr "distributionUrl" "%WRAPPER_PROPS%"') do set DISTRIBUTION_URL=%%A

:: Derive archive name and dir
for %%F in ("%DISTRIBUTION_URL%") do set MAVEN_ARCHIVE=%%~nxF
set MAVEN_DIR=%MAVEN_ARCHIVE:-bin.zip=%
set M2_HOME=%USERPROFILE%\.m2\wrapper\dists\%MAVEN_DIR%

:: Download if not cached
if not exist "%M2_HOME%" (
    echo Downloading Maven %MAVEN_DIR% ...
    mkdir "%M2_HOME%"
    powershell -Command "Invoke-WebRequest -Uri '%DISTRIBUTION_URL%' -OutFile '%M2_HOME%\%MAVEN_ARCHIVE%'"
    echo Extracting Maven ...
    powershell -Command "Expand-Archive -Path '%M2_HOME%\%MAVEN_ARCHIVE%' -DestinationPath '%M2_HOME%'"
    del "%M2_HOME%\%MAVEN_ARCHIVE%"
    echo Maven downloaded to: %M2_HOME%
)

:: Find mvn.cmd
set "MAVEN_BIN=%M2_HOME%\%MAVEN_DIR%\bin\mvn.cmd"
if exist "%MAVEN_BIN%" goto :found

for /d %%D in ("%M2_HOME%\apache-maven-*") do (
    if exist "%%D\bin\mvn.cmd" (
        set "MAVEN_BIN=%%D\bin\mvn.cmd"
        goto :found
    )
)
echo Error: Could not find mvn.cmd in %M2_HOME%
exit /b 1

:found
call "%MAVEN_BIN%" %*
exit /b %errorlevel%
