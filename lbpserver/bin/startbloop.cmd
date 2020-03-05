SET scrpath=%~dp0
call %scrpath%\setenv.cmd
cd %scrpath%\..\workspace\.bloop
call bloop ng-stop
bloop server
