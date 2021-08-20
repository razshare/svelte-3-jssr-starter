package net.razshare.svelte3jssr.callbacks;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.razshare.svelte3jssr.models.SvelteComponentResult;
import net.razshare.svelte3jssr.proxies.SvelteProxy;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

public class HttpRequestCallback {
    
    
    private static final String WD = Paths.get("").toAbsolutePath().toString()+"/";
    private static final TemplatesMap templates = new TemplatesMap();

    private static class TemplatesMap extends HashMap<String,String>{
        private final Pattern VARS = Pattern.compile("(?<!\\\\)\\$\\{\\w+\\}");
        public String getResolved(String key, Map<String,String> vars){
            String contents = get(key);
            Matcher varsMatcher = VARS.matcher(contents);

            return varsMatcher.replaceAll((match)->{
                String 
                    variableName = match.group();
                    variableName = variableName.substring(2,variableName.length()-1);

                if(vars.containsKey(variableName))
                    return vars.get(variableName);

                return String.format("<!-- Variable '%s' not found -->", variableName);
            });
        }
    }

    private static class Response{
        private final Value res;
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

    static {
        populateTemplatesFrom(templates, WD, "/src/main/templates", "/");
    }

    private static void populateTemplatesFrom(Map<String,String> templates, String WD, String root, String subdir) {
        WD = WD.replaceAll("\\\\+", "/").replaceFirst("\\/$", "");
        root = root.replaceAll("\\+", "/").replaceFirst("\\/$", "");
        subdir = subdir.replaceAll("\\+", "/").replaceFirst("\\/$", "");

        if(!subdir.startsWith("/"))
            subdir = "/"+subdir;

        if(!root.startsWith("/"))
            root = "/"+root;

        File dir = new File(WD+root+subdir);
        System.out.println("Iterating tempaltes from:"+WD+root+subdir);
        File[] directoryListing = dir.listFiles();
        //this checks if the file is a directory
        if (directoryListing != null)  for (File file : directoryListing) {
            String name = file
                            .getName()
                            .replaceAll("\\\\+", "/");

            String fullname = file
                                .getAbsolutePath()
                                .replaceAll("\\\\+", "/");

            if(name.toLowerCase().endsWith(".html")){
                try {
                    String key = fullname.replaceFirst(WD+root, "");
                    String contents = Files.readString(Path.of(fullname), StandardCharsets.UTF_8);
                    System.out.println("Adding template key:"+key);
                    templates.put(key, contents);
                } catch (IOException ex) {
                    Logger.getLogger(HttpRequestCallback.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else if(file.isDirectory()){
                populateTemplatesFrom(templates, WD, root, fullname);
            }
        }
    }

    public static void serveSsr(Value request, Value response) {
        Response res = new Response(response);
        res.writeHead(200,"OK");
        System.out.println("responding...");
        SvelteProxy.compile(WD+"/src/main/svelte/App.svelte", "ssr", (source)->{
            System.out.println("done!");
            SvelteComponentResult result = SvelteProxy.render(source,"ssr");
            String head = result.getHead();
            String body = result.getHtml();
            String css = result.getCssCode();
            String js = result.getJsCode();
            String index = templates.getResolved("/index.html", new HashMap<String,String>(){{
                put("HEAD", head);
                put("BODY", body);
                put("CSS", !css.isEmpty()?String.format("<style>%s</style>",css):"");
                put("JS", !js.isEmpty()?String.format("<script type='text/javascript'>%s</script>",js):"");
            }});
            res.end(index);
        });
    }

    public static void serveDom(Value request, Value response) {
        Response res = new Response(response);
        res.writeHead(200,"OK");
        System.out.println("responding...");
        SvelteProxy.compile(WD+"/src/main/svelte/App.svelte", "dom", (source)->{
            System.out.println("done!");
            
            // SvelteComponentResult result = SvelteProxy.render(source, "dom");
            // String head = result.getHead();
            // String body = result.getHtml();
            // String css = result.getCssCode();
            // String js = result.getJsCode();
            // String index = templates.getResolved("/index.html", new HashMap<String,String>(){{
            //     put("HEAD", head);
            //     put("BODY", body);
            //     put("CSS", !css.isEmpty()?String.format("<style>%s</style>",css):"");
            //     put("JS", !js.isEmpty()?String.format("<script type='text/javascript'>%s</script>",js):"");
            // }});

            //res.end(index);
            res.end(source);
        });
    }

    public static void run(Value request, Value response){
        //serveSsr(request, response);
        serveDom(request, response);
    }
}