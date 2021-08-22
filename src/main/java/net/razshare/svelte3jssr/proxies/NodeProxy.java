package net.razshare.svelte3jssr.proxies;

import java.nio.file.Paths;
import java.util.HashMap;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

public class NodeProxy {
    private static Value require;
    private static Value json;

    private static final String WD = Paths.get("").toAbsolutePath().toString()+"/";

    /**
     * Require a file using the NodeJS api.
     * @param filename Name of the file you want to require, by default this name is relative to the project working directory.
     */
    public static Value require(String filename){
        return require(filename,true);
    }

    /**
     * Require a file using the NodeJS api.
     * @param filename Name of the file you want to require.
     * @param scopedToProject if true will require the <b>filename</b> relative to 
     * the working directory of the project (aka location of pom.xml and package.json), 
     * otherwise <b>filename</b> will be relative to the Java entry point class (App.java).
     */
    public static Value require(String filename, boolean scopedToProject){
        Value object = require.execute(String.format("%s%s", scopedToProject?WD:"/",filename));
        return object;
    }

    public static Object JSONParse(String input){
        return json.invokeMember("parse", input).asHostObject();
    }

    public static String JSONStringify(HashMap<String,Object> object){
        return json.invokeMember("stringify", ProxyObject.fromMap(object)).asString();
    }

    /**
     * Set the proxy "require" function.<br />
     * This should be set to the NodeJS "require" function.
     */
    public static void setRequire(Value f){
        require = f;
    }

    public static void setJson(Value o){
        json = o;
    }

}
