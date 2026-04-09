#!/bin/bash
# ============================================================
#  build.sh - Compile and run the Online Quiz System
# ============================================================

echo ""
echo "  +----------------------------------------------+"
echo "  |     Online Quiz & Assessment System          |"
echo "  |           Build Script                       |"
echo "  +----------------------------------------------+"
echo ""

# Check for Java
if ! command -v javac &> /dev/null; then
    echo "[ERROR] javac not found. Please install JDK 11+."
    echo "        Ubuntu/Debian : sudo apt install default-jdk"
    echo "        macOS         : brew install openjdk"
    echo "        Windows       : https://adoptium.net/"
    exit 1
fi

echo "[INFO] Java version:"
java -version
echo ""

# Create output and data dirs
mkdir -p out data

# Compile
echo "[INFO] Compiling sources..."
javac -d out -sourcepath src \
    src/Main.java \
    src/model/Question.java \
    src/model/User.java \
    src/model/QuizResult.java \
    src/service/DataStore.java \
    src/service/QuestionBank.java \
    src/service/AuthService.java \
    src/service/QuizEngine.java \
    src/util/ConsoleUtil.java \
    src/ui/AdminPanel.java \
    src/ui/QuizUI.java

if [ $? -ne 0 ]; then
    echo "[ERROR] Compilation failed. See errors above."
    exit 1
fi

echo "[SUCCESS] Compilation complete!"
echo ""
echo "[INFO] Starting Quiz System..."
echo ""

# Run from project root so data/ directory resolves correctly
java -cp out Main
