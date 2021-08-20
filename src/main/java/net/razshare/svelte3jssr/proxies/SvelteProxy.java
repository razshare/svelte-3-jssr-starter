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
    private static Value render;

    public static SvelteClientSideComponentResult render(String source, String type){
        return render(source, type, new HashMap<String,Object>(){});
    }
    public static SvelteClientSideComponentResult render(String source, String type, Map<String,Object> options){
        Value component = render.execute(source, type, ProxyObject.fromMap(options));

        System.out.println(component.toString());

        String jsCode = component.hasMember("js")?component.getMember("js").getMember("code").asString():"";
        String cssCode = component.hasMember("css")?component.getMember("css").getMember("code").asString():"";
        String head = component.getMember("head").asString();
        String html = component.getMember("html").asString();
        return new SvelteClientSideComponentResult(jsCode,cssCode,head,html);
    }

    public static void compile(String source, String generate, Consumer<String> callback){
        compile(source, generate, new HashMap<String, Object>(){}, callback);
    }

    public static void compile(String source, String generate, Map<String,Object> options, Consumer<String> callback){
        Value compiled = compiler.execute(source, generate);
        compiled
        .invokeMember("then", (Consumer<String>) (result)->{
            callback.accept(result);
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

        return new SvelteServerSideComponentResult(
            css,
            head,
            html
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
    
    public static void setRender(Value f){
        render = f;
    }
}
