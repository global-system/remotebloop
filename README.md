# RemoteBloop
A set of tools to make easier adding remote compilation to the bloop server
## Attention 
The project is in experimental stage. 

See also: https://github.com/scalacenter/bloop/issues/1091
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
- Unpack `target\universal\remotebloop-*.*.*-SNAPSHOT.zip` to some folder
- Run `remotebloop-*.*.*-SNAPSHOT\bin\install.cmd`
  >It creates `remotebloop-*.*.*-SNAPSHOT\workspase` which contains bloop server. 
- Run `remotebloop-*.*.*-SNAPSHOT\bin\addservice.cmd`
  >It adds an windows service which name is BloopService
  
### Preparing to use bloop client 
- Start BloopService
- Add `remotebloop-*.*.*-SNAPSHOT\workspace\.bloop` to the path variable



