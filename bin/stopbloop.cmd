SET scrpath=%~dp0
call %scrpath%\setenv.cmd
cd %scrpath%\..\workspace\.bloop
bloop ng-stop
cd %scrpath%