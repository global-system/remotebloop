package ru.bitec.remotebloop.rbpcommander;

import ch.epfl.scala.bsp4j.*;
import org.eclipse.lsp4j.jsonrpc.Launcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.ProcessBuilder;

public class BloopConnection {
    public static interface RichBuildServer extends BuildServer, ScalaBuildServer, JvmBuildServer{

    }
    final Path workspace;
    BloopConnection(Path workspace){
        this.workspace = workspace;
    }
    private Process process;
    private BuildClient_ buildClient;
    private ExecutorService executorService;
    private RichBuildServer buildServer;
    private static class BuildClient_ implements BuildClient {
        @Override
        public void onBuildShowMessage(ShowMessageParams params) {

        }
        @Override
        public void onBuildLogMessage(LogMessageParams params) {
            System.out.println("onBuildLogMessage1:"+params.toString());
        }
        @Override
        public void onBuildTaskStart(TaskStartParams params) {

        }
        @Override
        public void onBuildTaskProgress(TaskProgressParams params) {

        }
        @Override
        public void onBuildTaskFinish(TaskFinishParams params) {

        }
        @Override
        public void onBuildPublishDiagnostics(PublishDiagnosticsParams params) {

        }
        @Override
        public void onBuildTargetDidChange(DidChangeBuildTarget params) {

        }
        @Override
        public void onConnectWithServer(BuildServer server) {

        }
    }

    public synchronized void  open() throws IOException {
        process = new ProcessBuilder("cmd","/Q","/c","experimental\\bin\\bloopbsp.cmd").redirectErrorStream(true).start();
        buildClient = new BuildClient_();
        executorService = Executors.newFixedThreadPool(1);
        Launcher<RichBuildServer> launcher = new Launcher.Builder<RichBuildServer>()
                .setOutput(process.getOutputStream())
                .setInput(process.getInputStream())
                .setLocalService(buildClient)
                .setExecutorService(executorService)
                .setRemoteInterface(RichBuildServer.class)
                .create();
        new Thread( () -> {
            try {
                System.out.println("start bloop listening");
                launcher.startListening().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                System.out.println("stop bloop listening");
            }
        }).start();
        buildServer = launcher.getRemoteProxy();
        ArrayList<String> buildCapabilities = new ArrayList<String>();
        buildCapabilities.add("scala");
        buildCapabilities.add("java");
        CompletableFuture<InitializeBuildResult> initializeResult = buildServer.buildInitialize(new InitializeBuildParams(
                "bspcommander", // name of this client
                "1.0.0", // version of this client
                "2.0.0", // BSP version
                workspace.toUri().toString(),
                new BuildClientCapabilities(buildCapabilities)
        ));
        initializeResult.thenAccept((a) ->
                buildServer.onBuildInitialized()
        ).join();
    }
    public synchronized RichBuildServer getBuildServer(){
        return buildServer;
    }
    public synchronized void close() throws Exception {
        buildServer.buildShutdown().thenAccept((a) -> {
            buildServer.onBuildExit();
        }).join();
        process.destroy();
        process.waitFor();
        executorService.shutdown();
    }
}
