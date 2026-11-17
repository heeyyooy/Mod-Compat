@echo off
setlocal

call "%~dp0gradlew.bat" clean build --no-daemon
if errorlevel 1 exit /b %ERRORLEVEL%

echo.
echo Build completed successfully.
echo JAR: build\libs\Mod-Compat-NeoForge-1.21.1-1.0.0.jar
