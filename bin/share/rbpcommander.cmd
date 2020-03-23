set proj_dir=%CD%
call %~dp0setenv.cmd
cd /d %~dp0../..
cmd /c java -cp lib\rbpcommander\* ru.bitec.remotebloop.rbpcommander.RbpCommander --config-dir "%proj_dir%\.bloop" %*
cd /d "%proj_dir%"