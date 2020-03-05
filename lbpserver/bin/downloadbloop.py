import urllib2

url = 'https://github.com/scalacenter/bloop/releases/download/v1.4.0-RC1/install.py'
response = urllib2.urlopen(url)
html = response.read()
file = open('install.py', 'wb')
file.write(html)
file.close()
