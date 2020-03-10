SET scrpath=%~dp0
call %scrpath%\setenv.cmd
cd /d %scrpath%\..
java -cp lib\* ru.bitec.remotebloop.rbpserver.HttpRBPServer