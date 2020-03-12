call %~dp0install\installbloop.cmd
call %~dp0install\installdaemon.cmd
call %~dp0setenv.cmd
cd /d  %~dp0..
mkdir workspace\.rbpserver\lock
cd /d %~dp0