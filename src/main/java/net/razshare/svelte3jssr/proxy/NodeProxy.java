package net.razshare.svelte3jssr.proxy;

import java.nio.file.Paths;
import org.graalvm.polyglot.Value;

public class NodeProxy {
    private static Value require;
    private static Value svelte;

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

    public static void setRequire(Value f){
        require = f;
    }
}
