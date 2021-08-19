package net.razshare.svelte3jssr;

import net.razshare.svelte3jssr.proxy.SvelteProxy;

public class App 
{
    private String name = "world";

    /**
     * Entry point of your Java application.
     */
    public App(){
        SvelteProxy.require("src/main/svelte/App.svelte");
        System.out.println("Java context started.");
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
