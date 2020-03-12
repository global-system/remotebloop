call %~dp0..\config\sethome.cmd
cd %~dp0..\workspace\.bloop
set bloop_path=%CD%
cd /D %~dp0
set PATH=%JAVA_HOME%\bin;%bloop_path%;%PYTHON_HOME%;%PATH%
