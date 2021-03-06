package ru.bitec.remotebloop.rbpserver;
import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;


public class RbpHtmlServer {
    public static class Args {
        @Parameter(names = { "--port", "-p" }, description = "http port")
        private Integer port = 0;
        @Parameter(names = "--help", help = true)
        private boolean help;
        @Parameter(names = "--stop", description = "stops server")
        private boolean stop=false;
    }
    public static final Path fileLockPath = Paths.get("workspace/.rbpserver/lock/port.txt");
    public static final CompletableFuture<Void> serverWork = new CompletableFuture<>();
    public static void main(String[] argArray) throws IOException {
        Args args = new Args();
        JCommander.newBuilder()
                .addObject(args)
                .build()
                .parse(argArray);
        if (args.help) return;
        if (args.stop){
            stop();
        }else{
            start(args);
        }

    }
    public static void start(Args args) throws IOException{
        HttpServer httpServer = createHttpServer(args.port);
        try(FileChannel fch = FileChannel.open(fileLockPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.SYNC)){
            System.out.println("Starting RbpHtmlServer...");
            httpServer.start();
            BloopManager$.MODULE$.startBloop();
            int port = httpServer.getListener("grizzly").getPort();
            ByteBuffer bb = ByteBuffer.allocate(100);
            bb.put(Integer.toString(port).getBytes());
            bb.flip();
            fch.write(bb);
            fch.force(true);
            System.out.println("Started RbpHtmlServer");
            System.out.println("see:");
            System.out.println("http://localhost:"+port+"/html/monitor.html");
            System.out.println("http://localhost:"+port+"/api/stopserver");
            try {
                serverWork.join();
            } catch (Exception e) {
                serverWork.cancel(false);
            }
            BloopManager$.MODULE$.shutDown();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            httpServer.shutdownNow();
        }
        //noinspection ResultOfMethodCallIgnored
        fileLockPath.toFile().delete();
    }
    public static void stop() throws IOException{
       String  content = new String ( Files.readAllBytes( fileLockPath ) );
       int port = Integer.valueOf(content);
       String stopString = "http://localhost:"+port+"/api/stopserver";
        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
            final HttpGet httpget = new HttpGet(stopString);
            CloseableHttpResponse response1 = httpclient.execute(httpget);
            String text = new String(IOUtils.toByteArray(response1.getEntity().getContent()));
            System.out.println(text);
        }
    }
    private static HttpServer createHttpServer(int port) {
        ResourceConfig rc = new ResourceConfig(RbpHtmlServerApi.class, RbpHtmlServerHtml.class);
        return GrizzlyHttpServerFactory.createHttpServer(URI.create("http://localhost:"+port+"/"), rc);
    }
}
