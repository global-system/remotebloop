call %~dp0setenv.cmd
cd /d %~dp0..
java -cp lib\rbpserver\* ru.bitec.remotebloop.rbpserver.RbpHtmlServer --stop