@echo off
title AXLᴇ Ghost Engine Loader
color 0b
echo ==========================================
echo    AXLᴇ CORPORATION - SYSTEM LOADING
echo ==========================================
echo [AI] Coffee RES: Brewing fresh code...
echo [SYSTEM] Compiling NexusCore...

javac -cp ".;lib/*" NexusCore.java

if %errorlevel% neq 0 (
    echo [ERROR] Critical failure during compilation.
    echo [HELP] Check if 'lib/jnativehook-2.2.2.jar' exists.
    pause
    exit
)

echo [SUCCESS] Engine ready. Launching...
java -cp ".;lib/*" NexusCore
pause