require('svelte/register')
const svelte = require('svelte/compiler');
const http = require("http")
const App = Java.type("net.razshare.svelte3jssr.App")
const NodeProxy = Java.type("net.razshare.svelte3jssr.proxy.NodeProxy")
const SvelteProxy = Java.type("net.razshare.svelte3jssr.proxy.SvelteProxy")

console.log("working dir from nodejs:",process.cwd());

// This following instructions give Java access to some NodeJS functions and their context.
// API starts
NodeProxy.setRequire(require)
SvelteProxy.setCompiler(svelte.compile)
// API ends

const app = new App()

http.createServer(function (request, response) {
    response.writeHead(200, {"Content-Type": "text/html"});
    response.end("Hello Graal.js");
}).listen(8000, function() {
	console.log("Graal.js server running at http://127.0.0.1:8000/")
})