# RemoteBloop
A set of tools to make easier adding remote compilation to the bloop server
## Attention 
The project is in experimental stage. 

See also: https://github.com/scalacenter/bloop/issues/1091
## Project goals
- run bloop as a service in windows 
- start\stop a bloop server by http api
- save a bloop state to zip
- restore bloop state by the zip with remote cache

The project does not aim to create infrastructure to manage jars with remote cache.

## Subprojects

### RbpServer
This project allows to run bloop as a service in windows.
### RbpCommander 
This project allows to save\restore a bloop state.

## Configuration
The `config` folder contains files which allows to configure projects
### sethome 
It configures paths: 
 - java home 
 - python home
### shareddirs.json
It configures shared directories which can be used to remap paths in save\restore a bloop state. 
The mapper try to use `project.resolution` (see https://scalacenter.github.io/bloop/docs/configuration-format ),
if it cannot be used the mapper will use `shareddirs`.

## How to use 
### Installation
- Download or clone project 
- Run sbt universal:packageBin 
- Unpack `target\universal\remotebloop-*.*.*-SNAPSHOT.zip` to some folder
- configure paths in `remotebloop-*.*.*-SNAPSHOT\config`
- Run `remotebloop-*.*.*-SNAPSHOT\bin\install.cmd`
  >It creates `remotebloop-*.*.*-SNAPSHOT\workspase` which contains bloop server. 
- Run `remotebloop-*.*.*-SNAPSHOT\bin\addservice.cmd`
  >It adds an windows service which name is BloopService
  
### Preparing to use bloop client 
- Start BloopService
- Add `remotebloop-*.*.*-SNAPSHOT\workspace\.bloop` to the path variable

### Preparing to use blpcommander 
- Add `remotebloop-*.*.*-SNAPSHOT\bin\share` to the path variable

### Usage of blpcommander
 - set current directory to the folder where `.bloop` is located
 - execute blpcommander command 
   > ```
   > Options:
   >   --help
   >     Prints the usage
   > Commands:
   >   save      Saves compilation state
   >     Usage: save [options]
   >       Options:
   >       * --target-dir
   >           File path to the directory where the state will be saved
   >  
   >   restore      Restores compilation state
   >     Usage: restore [options]
   >       Options:
   >       * --source-dir
   >           File path to the directory where the state will be restored from
   > ```
#### Attention 
You should stop bloop server manually before blpcommander usage.

## Roadmap
 - make incremental save\restore
 - automate synchronization bloop state with blpcommander
 - fix bugs on usage 
 - issue stable release