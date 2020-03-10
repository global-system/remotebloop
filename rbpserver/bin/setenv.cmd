set scrpath=%~dp0
call %scrpath%..\config\sethome.cmd
cd %scrpath%..\workspace\.bloop
set bloop_path=%CD%
cd %scrpath%
set PATH=%JAVA_HOME%\bin;%bloop_path%;%PYTHON_HOME%;%PATH%
