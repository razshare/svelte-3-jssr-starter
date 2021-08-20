package net.razshare.svelte3jssr.models;

import lombok.Getter;

public class SvelteServerSideComponentResult {
    
    @Getter private final String cssCode;
    @Getter private final String head;
    @Getter private final String html;

    /**
     * Represents a Svelte object in its html and css form.
     * @param cssCode raw css code.
     * @param head head html.
     * @param html component html.
     */
    public SvelteServerSideComponentResult(
        String cssCode,
        String head,
        String html
    ){
        this.cssCode = cssCode;
        this.head = head;
        this.html = html;
    }
}
