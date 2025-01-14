@echo off
setlocal

REM Set the path to the JDK
set JAVA_HOME="C:\Program Files\Java\jdk-23"
set PATH=%JAVA_HOME%\bin;%PATH%

REM Set the workspace folder
set WORKSPACE_FOLDER=%~dp0

REM Compile the Java files
for /r "%WORKSPACE_FOLDER%src" %%f in (*.java) do (
    javac -cp "%WORKSPACE_FOLDER%lib\opencv-4100.jar" -d "%WORKSPACE_FOLDER%bin" "%%f"
)

endlocal