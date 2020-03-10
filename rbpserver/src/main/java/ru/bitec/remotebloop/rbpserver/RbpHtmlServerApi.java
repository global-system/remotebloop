package ru.bitec.remotebloop.rbpserver;

import org.apache.commons.text.StringEscapeUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;
import java.util.Base64;


@Path("api")
public class RbpHtmlServerApi {
    private void sseSendMessage(SseEventSink eventSink, @Context Sse sse,String message){
        if (!eventSink.isClosed()){
            String data;
            data = StringEscapeUtils.escapeHtml4(message);
            data = Base64.getEncoder().encodeToString(data.getBytes());
            final OutboundSseEvent event = sse.newEventBuilder()
                    .name("data")
                    .data(String.class, data)
                    .build();
            eventSink.send(event);
        }
    }
    private void sseEnd(SseEventSink eventSink, @Context Sse sse){
        if (!eventSink.isClosed()) {
            String data;
            data = StringEscapeUtils.escapeHtml4("end sse");
            data = Base64.getEncoder().encodeToString(data.getBytes());
            final OutboundSseEvent event = sse.newEventBuilder()
                    .name("end")
                    .data(String.class, data)
                    .build();
            eventSink.send(event);
        }
    }
    @Path("getsse")
    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void getServerSentEvents(@Context SseEventSink eventSink, @Context Sse sse) {
        String info;
        if (BloopManager$.MODULE$.isBloopStarted()){
            info = "Bloop is working";
        } else {
            info = "Bloop is not working";
        }
        sseSendMessage(eventSink,sse,info);
        final BloopManagerListener bml = new BloopManagerListener() {
            @Override
            public void onShutDown() {
                sseSendMessage(eventSink,sse,"server is stopping");
                sseEnd(eventSink,sse);
            }

            @Override
            public void onLog(String line) {
                if (eventSink.isClosed()){
                    BloopManager$.MODULE$.unSubscribe(this);
                }else{
                    sseSendMessage(eventSink,sse,line);
                }
            }
        };
        BloopManager$.MODULE$.subscribe(bml);
    }
    @Path("stopserver")
    @GET
    public String stopserver() {
        RbpHtmlServer.serverWork.cancel(false);
        return "Server stopping";
    }
    @Path("stopbloop")
    @GET
    public String stopBloop() {
        BloopManager$.MODULE$.stopBloop();
        return "Bloop is stopping";
    }
    @Path("startbloop")
    @GET
    public String startBloop() {
        BloopManager$.MODULE$.startBloop();
        return "Bloop is starting";
    }
}
