setlocal enabledelayedexpansion
SET scrpath=%~dp0
call %scrpath%setenv.cmd
cd %scrpath%..
set root_path=%CD%
cd /d  %scrpath%

For /R ..\lib\rbpserver %%i IN (*.jar) do set classpath=!classpath!%%i;

"%scrpath%\..\workspace\commons-daemon\prunsrv.exe" //IS//BloopService ^
  --DisplayName="BloopService" ^
  --Install="%root_path%\workspace\commons-daemon\prunsrv.exe" ^
  --Startup="manual" ^
  --LogPath="%root_path%\workspace" ^
  --JavaHome="%JAVA_HOME%" ^
  --StartPath="%root_path%"	^
  --StopPath="%root_path%" ^
  --Classpath="%classpath%" ^
  --StartMode="java" ^
  --StartClass="ru.bitec.remotebloop.rbpserver.RbpHtmlServer" ^
  --StartParams="" ^
  --StopMode="java" ^
  --StopClass="ru.bitec.remotebloop.rbpserver.RbpHtmlServer" ^
  --StopParams="stop"
 