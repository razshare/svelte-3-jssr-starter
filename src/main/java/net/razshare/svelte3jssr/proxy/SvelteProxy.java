package net.razshare.svelte3jssr.proxy;

import java.util.HashMap;
import java.util.Map;
import net.razshare.svelte3jssr.model.SvelteComponentResult;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

public class SvelteProxy {
    private static Value compiler;


    public static Value compile(String source){
        return compile(source,new HashMap<String, Object>(){});
    }

    public static Value compile(String source, Map<String,Object> options){
        return compiler.execute(source,options);
    }
    
    public static void setCompiler(Value c){
        compiler = c;
    }



    public static SvelteComponentResult require(String filename){
        return require(filename, new HashMap<String,Object>(){});
    }

    public static SvelteComponentResult require(String filename, Map<String,Object> options){
        return require(filename, options, true);
    }

    public static SvelteComponentResult require(String filename, Map<String,Object> options, boolean scopeToProject){
        Value object = NodeProxy.require(filename,scopeToProject);
        Value defaults = object.getMember("default");
        Value render = defaults.getMember("render");
        Value result = render.execute(ProxyObject.fromMap(options));

        String head = result.getMember("head").asString();
        String html = result.getMember("html").asString();
        String css = result.getMember("css").getMember("code").asString();
        String cssMap = result.getMember("css").getMember("map").asString();

        return new SvelteComponentResult(
            head,
            html,
            css,
            cssMap
        );
    }
}
