@echo off

javac src/TestApp.java
java src/TestApp %ComputerName%:4455 BACKUP medium.txt 3
@PAUSE
java src/TestApp %ComputerName%:4455 RESTORE medium.txt 
@PAUSE
java src/TestApp %ComputerName%:4465 STATE
REM java src/TestApp %ComputerName%:4455 DELETE medium.txt 
@PAUSE
REM java src/TestApp %ComputerName%:4455 RECLAIM 0
@PAUSE
:loop
set /p id="Enter Command: "
%id%
goto loop