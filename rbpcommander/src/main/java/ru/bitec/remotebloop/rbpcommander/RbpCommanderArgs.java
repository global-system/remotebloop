package ru.bitec.remotebloop.rbpcommander;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

public class RbpCommanderArgs {
    @Parameter(names = "--help"
            ,description = "Prints the usage"
            , help = true
    )
    public boolean help;
    @Parameter(names = "--config-dir",description = "File path to the bloop config directory",required = true)
    public String configDir;
    @Parameters(commandDescription = "Saves compilation state")
    public static class Save{
        @Parameter(names = "--target-dir"
                ,description = "File path to the directory where the state will be saved"
                ,required = true)
        public String targetDir;
    }
    @Parameters(commandDescription = "Restores compilation state")
    public static class Restore{
        @Parameter(names = "--source-dir"
                ,description = "File path to the directory where the state will be restored from"
                ,required = true)
        public String sourceDir;
    }
}
