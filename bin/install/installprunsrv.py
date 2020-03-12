import urllib2
from zipfile import ZipFile
url = 'https://downloads.apache.org/commons/daemon/binaries/windows/commons-daemon-1.2.2-bin-windows.zip'
response = urllib2.urlopen(url)
html = response.read()
file = open('commons-daemon.zip', 'wb')
file.write(html)
file.close()

with ZipFile('commons-daemon.zip', 'r') as zipObj:
    # Extract all the contents of zip file in current directory
    zipObj.extractall("commons-daemon")