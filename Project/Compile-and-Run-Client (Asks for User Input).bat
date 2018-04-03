@echo off

javac src/TestApp.java
:loop
set /p id="Enter Command (java src/TestApp <peer_ap> <sub_protocol> <opnd_1> <opnd_2>):"
%id%
goto loop