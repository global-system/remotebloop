# RemoteBloop
A set of tools to make easier adding remote compilation to the bloop server

## Project goals
- run bloop as a service in windows 
- start\stop a bloop server by http api
- create a jar with remote cache 
- restore bloop state by the jar with remote cache

The project does not aim to create infrastructure to manage jars with remote cache

## How to use 

### Installation
- Download or clone project 
- Run sbt universal:packageBin 
- Unpack `rbpserver\target\universal\rbpserver-*.*.*-SNAPSHOT.zip` to some folder
- Run `rbpserver-*.*.*-SNAPSHOT\bin\install.cmd`
  >It creates `rbpserver-*.*.*-SNAPSHOT\workspase` which contains bloop server. 
- Run `rbpserver-*.*.*-SNAPSHOT\bin\addservice.cmd`
  >It adds an windows service which name is BloopService
  
### Preparing to use bloop client 
- Start BloopService
- Add `rbpserver-*.*.*-SNAPSHOT\workspace\.bloop` to the path variable



