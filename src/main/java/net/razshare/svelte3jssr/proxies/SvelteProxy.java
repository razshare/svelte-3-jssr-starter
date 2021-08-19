package net.razshare.svelte3jssr.proxies;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.razshare.svelte3jssr.models.SvelteClientSideComponentResult;
import net.razshare.svelte3jssr.models.SvelteServerSideComponentResult;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

public class SvelteProxy {
    private static Value compiler;
    private static Value unwrapJs;


    public static void compile(String source, Consumer<SvelteClientSideComponentResult> callback){
        compile(source, new HashMap<String, Object>(){}, callback);
    }

    public static void compile(String source, Map<String,Object> options, Consumer<SvelteClientSideComponentResult> callback){
        Value compiled = compiler.execute(source,ProxyObject.fromMap(options));
        compiled
        .invokeMember("then", (Consumer<Map<String,Map<String,String>>>) (result)->{
            callback.accept(new SvelteClientSideComponentResult(
                result.get("js").get("code"),
                result.get("css").get("code")
            ));
        }).invokeMember("catch", (Consumer<Object>) (result)->{
            System.out.println("Some error occoured:\n"+result.toString());
        });
        
    }

    public static SvelteServerSideComponentResult require(String filename){
        return require(filename, new HashMap<String,Object>(){});
    }

    public static SvelteServerSideComponentResult require(String filename, Map<String,Object> options){
        return require(filename, options, true);
    }

    public static SvelteServerSideComponentResult require(String filename, Map<String,Object> options, boolean scopeToProject){
        Value object = NodeProxy.require(filename,scopeToProject);
        Value defaults = object.getMember("default");
        Value render = defaults.getMember("render");
        Value result = render.execute(ProxyObject.fromMap(options));

        String head = result.getMember("head").asString();
        String html = result.getMember("html").asString();
        String css = result.getMember("css").getMember("code").asString();
        String cssMap = result.getMember("css").getMember("map").asString();

        return new SvelteServerSideComponentResult(
            head,
            html,
            css,
            cssMap
        );
    }

    public static String unwrapJs(){
        return unwrapJs.execute().asString();
    }

    public static void setCompiler(Value c){
        compiler = c;
    }

    public static void setUnwrapJs(Value f){
        unwrapJs = f;
    }
}
