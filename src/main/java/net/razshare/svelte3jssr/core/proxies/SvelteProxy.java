package net.razshare.svelte3jssr.core.proxies;

import java.util.HashMap;
import java.util.function.Consumer;
import net.razshare.svelte3jssr.core.models.SvelteComponentResult;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

public class SvelteProxy {
    private static Value compiler;
    private static Value render;

    /**
     * Parse a svelte input source code into a SvelteComponentResult object.
     * @param compiledSource The compiled svelte source code.<br>
     * This is not the raw svelte source code, you must first pass your raw svelte code through the compiler.
     * @return The parsed SvelteComponentResult.
     */
    public static SvelteComponentResult render(String compiledSource){
        return render(compiledSource, new HashMap<>());
    }

    /**
     * Parse a compiled svelte input source code into a SvelteComponentResult object.
     * @param compiledSource The compiled svelte source code.<br>
     * This is not the raw svelte source code, you must first pass your raw svelte code through the compiler.
     * @param props Properties to pass to the svelte component.
     * @return The parsed SvelteComponentResult.
     */
    public static SvelteComponentResult render(String compiledSource, HashMap<String,Object> props){
        Value component = render.execute(compiledSource, ProxyObject.fromMap(props));
        String jsCode = component.hasMember("js")?component.getMember("js").getMember("code").asString():"";
        String cssCode = component.hasMember("css")?component.getMember("css").getMember("code").asString():"";
        String head = component.getMember("head").asString();
        String html = component.getMember("html").asString();
        return new SvelteComponentResult(jsCode,cssCode,head,html);
    }

    /**
     * Compile a raw svelte input source code into it's javascript form.<br>
     * The compilation is executed asynchronously.
     * @param rawSourceFileName The raw svelte source code.
     * @param generate Type of compilation.<br>
     * This should be either "dom" or "ssr".
     * @param callback The callback that will be invoked when the compilation is completed.
     */
    public static void compile(String rawSourceFileName, String generate, Consumer<String> callback){
        Value compiled = compiler.execute(rawSourceFileName, generate);
        compiled
        .invokeMember("then", (Consumer<String>) (result)->{
            callback.accept(result);
        }).invokeMember("catch", (Consumer<Object>) (result)->{
            System.out.println("Some error occoured:\n"+result.toString());
        });
        
    }
    
    
    public static void ssr(String rawSourceFileName, Consumer<String> callback) {
        ssr(rawSourceFileName,new HashMap<>(),callback);
    }
    
    
    public static void ssr(String rawSourceFileName, HashMap<String,Object> props, Consumer<String> callback) {
        SvelteProxy.compile(rawSourceFileName, "ssr", (sourceSSR)->{

            SvelteComponentResult result = SvelteProxy.render(sourceSSR,props);
            String head = result.getHead();
            String body = result.getHtml();
            String css = result.getCssCode();
            
            SvelteProxy.compile(rawSourceFileName, "dom", (sourceDOM)->{
                
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
    
    public static void setCompiler(Value c){
        compiler = c;
    }
    
    public static void setRender(Value f){
        render = f;
    }
}
