@echo off
:: ============================================================
::  build.bat - Compile and run the Online Quiz System (Windows)
:: ============================================================

echo.
echo   Online Quiz ^& Assessment System
echo   Build Script (Windows)
echo.

where javac >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] javac not found. Please install JDK 11+ and add it to PATH.
    echo         Download: https://adoptium.net/
    pause
    exit /b 1
)

echo [INFO] Java version:
java -version
echo.

if not exist out mkdir out
if not exist data mkdir data

echo [INFO] Compiling sources...

javac -d out -sourcepath src ^
    src\Main.java ^
    src\model\Question.java ^
    src\model\User.java ^
    src\model\QuizResult.java ^
    src\service\DataStore.java ^
    src\service\QuestionBank.java ^
    src\service\AuthService.java ^
    src\service\QuizEngine.java ^
    src\util\ConsoleUtil.java ^
    src\ui\AdminPanel.java ^
    src\ui\QuizUI.java

if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed. See errors above.
    pause
    exit /b 1
)

echo [SUCCESS] Compilation complete!
echo.
echo [INFO] Starting Quiz System...
echo.

java -cp out Main
pause
