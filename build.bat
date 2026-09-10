@echo off
REM 构建辅助脚本：显式指定 JDK 与 Android SDK，避免依赖系统环境变量。
chcp 65001 > nul
set "JAVA_HOME=D:\刘浩\Android\jbr"
set "ANDROID_HOME=D:\sdk"
set "ANDROID_SDK_ROOT=D:\sdk"

REM 关键：Gradle 用户目录与系统 TEMP 均位于中文路径下，会导致测试工作进程
REM 出现 ClassNotFoundException: GradleWorkerMain，故统一改用 ASCII 路径。
REM C:\gradle-home 是指向 %USERPROFILE%\.gradle 的目录联接，可复用全部依赖缓存。
set "GRADLE_USER_HOME=C:\gradle-home"
set "TEMP=C:\gradle-tmp"
set "TMP=C:\gradle-tmp"
if not exist "C:\gradle-tmp" mkdir "C:\gradle-tmp"

cd /d "%~dp0"

echo [build] JAVA_HOME=%JAVA_HOME%
echo [build] ANDROID_HOME=%ANDROID_HOME%
echo [build] TEMP=%TEMP%
echo [build] GRADLE_USER_HOME=%GRADLE_USER_HOME%

set "GRADLE_BIN=C:\gradle-home\wrapper\dists\gradle-8.13-bin\5xuhj0ry160q40clulazy9h7d\gradle-8.13\bin\gradle.bat"

if exist "%GRADLE_BIN%" (
    call "%GRADLE_BIN%" %*
) else (
    call "%~dp0gradlew.bat" %*
)



