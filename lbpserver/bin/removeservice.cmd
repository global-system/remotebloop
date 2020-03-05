SET scrpath=%~dp0
call %scrpath%setenv.cmd
cd /d  %scrpath%
"%scrpath%\..\workspace\commons-daemon\prunsrv.exe" //DS//BloopService
