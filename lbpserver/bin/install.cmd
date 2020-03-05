SET scrpath=%~dp0
call %scrpath%setenv.cmd
cd /d  %scrpath%
cd /d  ..
mkdir workspace\.bloop
mkdir workspace\.lbpserver\lock
cd /d %scrpath%..\workspace\.bloop
python %scrpath%downloadbloop.py
python install.py -d %scrpath%..\workspace\.bloop
cd /d %scrpath%..\workspace
python %scrpath%installprunsrv.py

cd /d %scrpath%