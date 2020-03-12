call %~dp0..\setenv.cmd
mkdir %~dp0..\..\workspace\.bloop
cd /D %~dp0..\..\workspace\.bloop
python %~dp0downloadbloop.py
python install.py -d %~dp0..\..\workspace\.bloop