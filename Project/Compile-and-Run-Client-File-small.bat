@echo off

javac src/TestApp.java
java src/TestApp %ComputerName%:4455 BACKUP small.txt 3
@PAUSE
java src/TestApp %ComputerName%:4455 RESTORE small.txt
@PAUSE
java src/TestApp %ComputerName%:4455 DELETE small.txt
@PAUSE
java src/TestApp %ComputerName%:4455 RECLAIM 0
@PAUSE
:loop
set /p id="Enter Command: "
%id%
goto loop