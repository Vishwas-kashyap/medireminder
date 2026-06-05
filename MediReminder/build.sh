#!/bin/bash
# ============================================================
# build.sh – Compile and run MediReminder
# Usage:
#   chmod +x build.sh
#   ./build.sh          # compile + run
#   ./build.sh compile  # compile only
# ============================================================

# ---- CONFIGURE THIS ----
JDBC_JAR="lib/mysql-connector-java-8.0.33.jar"
SRC_DIR="src"
OUT_DIR="out"
MAIN_CLASS="Main"

# Create output directory
mkdir -p "$OUT_DIR"

echo "==> Compiling..."
find "$SRC_DIR" -name "*.java" > sources.txt

javac -cp "$JDBC_JAR" -d "$OUT_DIR" @sources.txt

if [ $? -ne 0 ]; then
    echo "==> Compilation FAILED."
    rm sources.txt
    exit 1
fi

rm sources.txt
echo "==> Compilation SUCCESS."

if [ "$1" == "compile" ]; then
    echo "==> Skipping run (compile only)."
    exit 0
fi

echo "==> Running MediReminder..."
java -cp "$OUT_DIR:$JDBC_JAR" "$MAIN_CLASS"
