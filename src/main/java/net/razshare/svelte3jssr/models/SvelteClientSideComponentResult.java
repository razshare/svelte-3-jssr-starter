package net.razshare.svelte3jssr.models;

import lombok.Getter;
import org.graalvm.polyglot.Value;

public class SvelteClientSideComponentResult {
    
    @Getter private final String jsCode;
    @Getter private final String cssCode;

    /**
     * Represents a Svelte object in its js and css form.
     * @param jsCode raw javascript code.
     * @param jsMap a sourcemap with additional toString() and toUrl() convenience methods.
     * @param cssCode raw css code.
     * @param cssMap a sourcemap with additional toString() and toUrl() convenience methods.
     */
    public SvelteClientSideComponentResult(
        String jsCode,
        String cssCode
    ){
        this.jsCode = jsCode;
        this.cssCode = cssCode;
    }
}
