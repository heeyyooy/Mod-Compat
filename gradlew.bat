@echo off
setlocal

rem Self-bootstrapping Gradle launcher for Windows.
rem Keeps the Gradle distribution in the user's Gradle cache.
set "GRADLE_VERSION=9.2.1"
if not defined GRADLE_USER_HOME set "GRADLE_USER_HOME=%USERPROFILE%\.gradle"
set "GRADLE_INSTALL_DIR=%GRADLE_USER_HOME%\wrapper\dists\gradle-%GRADLE_VERSION%-bin\manual\gradle-%GRADLE_VERSION%"

if not exist "%GRADLE_INSTALL_DIR%\bin\gradle.bat" (
    echo Gradle %GRADLE_VERSION% was not found. Downloading the Gradle distribution...
    powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference = 'Stop'; $version = $env:GRADLE_VERSION; $root = Join-Path $env:GRADLE_USER_HOME ('wrapper\dists\gradle-' + $version + '-bin\manual'); $archive = Join-Path $root ('gradle-' + $version + '-bin.zip'); New-Item -ItemType Directory -Path $root -Force | Out-Null; Invoke-WebRequest -Uri ('https://services.gradle.org/distributions/gradle-' + $version + '-bin.zip') -OutFile $archive; Expand-Archive -LiteralPath $archive -DestinationPath $root -Force; Remove-Item -LiteralPath $archive -Force"
    if errorlevel 1 (
        echo Failed to download or extract Gradle %GRADLE_VERSION%.
        exit /b 1
    )
)

call "%GRADLE_INSTALL_DIR%\bin\gradle.bat" %*
exit /b %ERRORLEVEL%
