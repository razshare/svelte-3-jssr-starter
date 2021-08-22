package net.razshare.svelte3jssr.proxies;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.razshare.svelte3jssr.models.SvelteComponentResult;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

public class SvelteProxy {
    private static Value compiler;
    private static Value unwrapJs;
    private static Value render;

    public static SvelteComponentResult render(String source){
        return render(source, new HashMap<>());
    }

    public static SvelteComponentResult render(String source, HashMap<String,Object> props){
        Value component = render.execute(source, ProxyObject.fromMap(props));
        String jsCode = component.hasMember("js")?component.getMember("js").getMember("code").asString():"";
        String cssCode = component.hasMember("css")?component.getMember("css").getMember("code").asString():"";
        String head = component.getMember("head").asString();
        String html = component.getMember("html").asString();
        return new SvelteComponentResult(jsCode,cssCode,head,html);
    }

    public static void compile(String source, String generate, Consumer<String> callback){
        Value compiled = compiler.execute(source, generate);
        compiled
        .invokeMember("then", (Consumer<String>) (result)->{
            callback.accept(result);
        }).invokeMember("catch", (Consumer<Object>) (result)->{
            System.out.println("Some error occoured:\n"+result.toString());
        });
        
    }

    public static void ssr(String filename, Consumer<String> callback) {
        ssr(filename,new HashMap<>(),callback);
    }

    public static void ssr(String filename, HashMap<String,Object> props, Consumer<String> callback) {
        SvelteProxy.compile(filename, "ssr", (sourceSSR)->{

            SvelteComponentResult result = SvelteProxy.render(sourceSSR,props);
            String head = result.getHead();
            String body = result.getHtml();
            String css = result.getCssCode();
            
            SvelteProxy.compile(filename, "dom", (sourceDOM)->{
                
                callback.accept(String.format(
                    "<!DOCTYPE html>"
                    +"<html lang='en'>"
                    +"<head>"
                        +"%s"
                        +"<style>%s</style>"
                    +"</head>"
                    +"<body>"
                        +"%s<br/>"
                        +"<script type='text/javascript'>%s</script>"
                    +"</body>"
                    +"</html>",
                    head,
                    css,
                    body,
                    String.format("((props)=>{window.Java={};const App = %s\ndocument.body.innerHTML = '';new App({target:document.body,props});})(%s)", sourceDOM, NodeProxy.JSONStringify(props).replaceAll("\"", "\\\""))
                ));
            });
        });
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
