package net.razshare.svelte3jssr.model;

import lombok.Getter;

public class SvelteComponentResult {
    
    @Getter private final String head;
    @Getter private final String html;
    @Getter private final String css;
    @Getter private final String cssMap;

    public SvelteComponentResult(
        String head,
        String html,
        String css,
        String cssMap
    ){
        this.head = head;
        this.html = html;
        this.css = css;
        this.cssMap = cssMap;
    }
}
