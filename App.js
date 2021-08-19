require('svelte/register')
//const fs = require('fs');
const svelte = require('svelte/compiler');
const http = require("http")
const { readFile } = require('fs');
const App = Java.type("net.razshare.svelte3jssr.App")
const NodeProxy = Java.type("net.razshare.svelte3jssr.proxies.NodeProxy")
const SvelteProxy = Java.type("net.razshare.svelte3jssr.proxies.SvelteProxy")
const JavaProxy = Java.type("net.razshare.svelte3jssr.proxies.JavaProxy")



// This following instructions give Java access to some NodeJS functions and their context.
// API starts
NodeProxy.setRequire(require)
SvelteProxy.setCompiler(async (source,options)=>{
    const {js,css} = svelte.compile(source,options)
    
    return {
        js,
        css
    }
})
// API ends

const app = new App()

http
.createServer((request, response)=>{
    JavaProxy.requestCallback(request, response)
})
.listen(8000, ()=>console.log("Graal.js server running at http://127.0.0.1:8000/"))