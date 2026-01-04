@echo off
setlocal
for /f "tokens=2 delims=:." %%x in ('chcp') do set _codepage=%%x
chcp 65001>nul
cd D:\Project\VoidDimension\run
D:\Java\jdk-21.0.6\bin\java.exe @D:\Project\VoidDimension\build\moddev\clientRunClasspath.txt @D:\Project\VoidDimension\build\moddev\clientRunVmArgs.txt -Dfml.modFolders=voiddimension%%%%D:\Project\VoidDimension\build\classes\java\main;voiddimension%%%%D:\Project\VoidDimension\build\resources\main net.neoforged.devlaunch.Main @D:\Project\VoidDimension\build\moddev\clientRunProgramArgs.txt
if not ERRORLEVEL 0 (  echo Minecraft failed with exit code %ERRORLEVEL%  pause)
chcp %_codepage%>nul
endlocal