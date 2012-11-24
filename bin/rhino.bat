@echo off
REM -opt -1 is needed for Envjs (interpretation, not compilation mode) 

REM Rhino JAR options: 	See https://developer.mozilla.org/en/Rhino_Shell 
REM EnvJS usage: 		See http://www.envjs.com/doc/guides#running-rhino

java -cp js.jar org.mozilla.javascript.tools.shell.Main -opt -1
