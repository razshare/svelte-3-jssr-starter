package net.razshare.svelte3jssr.callbacks;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import net.razshare.svelte3jssr.proxies.SvelteProxy;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

public class HttpRequestCallback {
    
    
    private static final String WD = Paths.get("").toAbsolutePath().toString();

    private static class Response{
        private final Value res;
        public Response(Value response){
            this.res = response;
        }

        public void end(String contents){
            res.getMember("end").execute(contents);
        }

        public void writeHead(int statusCode, String reasonPhrase){
            writeHead(statusCode, reasonPhrase, new HashMap<>());
        }
        public void writeHead(int statusCode, String reasonPhrase, Map<String,Object> headerFields){
            res.getMember("writeHead").execute(statusCode, reasonPhrase, ProxyObject.fromMap(headerFields));
        }
    }

    private static class Request{
        private final Value req;
        private String url;
        public Request(Value request){
            this.req = request;
            url = request.getMember("url").asString();

            if(url.endsWith("/") || "".equals(url))
                url = "/index.html";
        }

        public String getUrl(){
            return url;
        }
    }

    public static void run(Value request, Value response){
        Request req = new Request(request);
        Response res = new Response(response);
        
        res.writeHead(200,"OK", new HashMap<>(){{
            put("Content-Type","text/html");
        }});

        SvelteProxy.ssr(String.format("%s/src/main/svelte/App.svelte", WD), new HashMap<>(){{
            put("name", "world!");
        }},(result)->{
            res.end(result);
        });
    }
}