package ru.bitec.remotebloop.rbpcommander;


import ch.epfl.scala.bsp4j.*;

import java.util.Collections;

public class BloopConnectorExample {
    BloopConnection bc = new BloopConnection(
            java.nio.file.Paths.get("C:/vcs/git/globalsystem/remotebloop").toAbsolutePath().normalize()
    );
    String ti = "file:///C:/vcs/git/globalsystem/remotebloop?id=rbpcommander";
    void printScalaOption() throws Exception{
        ScalacOptionsResult or = bc.getBuildServer().buildTargetScalacOptions(
                new ScalacOptionsParams(Collections.singletonList(
                        new BuildTargetIdentifier(ti)
                ))
        ).get();
        or.getItems().forEach(i ->{
            System.out.println("ClassDirectory:"+i.getClassDirectory());
        });
    }

    void run() throws Exception{
        bc.open();
        printScalaOption();
        /*bc.getBuildServer().buildTargetCleanCache(
                new CleanCacheParams(Collections.singletonList(
                        new BuildTargetIdentifier(ti)
                ))
        ).get();*/
        printScalaOption();
        bc.getBuildServer().buildTargetCompile(
                new CompileParams(Collections.singletonList(
                        new BuildTargetIdentifier(ti)
                ))
        ).get();
        printScalaOption();
        bc.close();
    }
    public static void main(String[] args) throws Exception {
         new BloopConnectorExample().run();
    }
}
