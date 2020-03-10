package ru.bitec.remotebloop.rbpserver;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

@Path("html/{path:.+}")
public class RbpHtmlServerHtml {
    @GET
    @Produces({MediaType.TEXT_HTML})
    public InputStream getPrinter(@PathParam("path") String path) {
        String rootPath = "ru/bitec/remotebloop/rbpserver/"+path;
        InputStream is = getClass().getClassLoader().getResourceAsStream(rootPath);
        return is;
    }
}
