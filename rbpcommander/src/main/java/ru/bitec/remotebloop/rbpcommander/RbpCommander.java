package ru.bitec.remotebloop.rbpcommander;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class RbpCommander {
    public static class Args {
        @Parameter(names = "--save", description = "save compilation state to jar archive")
        private boolean needSave;
        @Parameter(names = "--load", description = "load compilation state from jar archive")
        private boolean needLoad;
        @Parameter(names = "--help", help = true)
        private boolean help;
    }
    public static final String fileLockName = "workspace\\.rbpserver\\lock\\port.txt";
    public static final CompletableFuture<Void> serverWork = new CompletableFuture<>();
    public static void main(String[] argArray) throws IOException {

    }
}
