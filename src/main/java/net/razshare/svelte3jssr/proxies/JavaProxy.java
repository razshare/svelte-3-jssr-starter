package net.razshare.svelte3jssr.proxies;

import java.io.IOException;
import net.razshare.svelte3jssr.callbacks.HttpRequestCallback;
import org.graalvm.polyglot.Value;

public class JavaProxy {
    public static void requestCallback(Value request, Value response) throws IOException{
        HttpRequestCallback.run(request, response);
    }    
}
