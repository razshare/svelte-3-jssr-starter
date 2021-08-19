package net.razshare.svelte3jssr.callbacks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import net.razshare.svelte3jssr.proxies.SvelteProxy;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

public class HttpRequestCallback {
    private static class Response{
        private Value res;
        public Response(Value response){
            this.res = response;
        }

        public void end(String contents){
            res.getMember("end").execute(contents);
        }

        public void writeHead(int statusCode, String reasonPhrase){
            writeHead(statusCode, reasonPhrase, new HashMap<String,Object>(){});
        }
        public void writeHead(int statusCode, String reasonPhrase, Map<String,Object> headerFields){
            res.getMember("writeHead").execute(statusCode, reasonPhrase, ProxyObject.fromMap(headerFields));
        }

    }

    public static void run(Value request, Value response) throws IOException{
        Response res = new Response(response);
        res.writeHead(200,"OK");
        String contents = Files.readString(Path.of("src/main/svelte/App.svelte"));
        System.out.println("responding...");
        SvelteProxy.compile(contents, (result)->{
            System.out.println("done!");
            res.end(result.getJsCode());
        });
    }
}