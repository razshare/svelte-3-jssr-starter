import svelte from 'rollup-plugin-svelte'
import resolve from '@rollup/plugin-node-resolve'
import http from "http"
import { rollup } from 'rollup';


const App = Java.type("net.razshare.svelte3jssr.App")
const NodeProxy = Java.type("net.razshare.svelte3jssr.proxies.NodeProxy")
const SvelteProxy = Java.type("net.razshare.svelte3jssr.proxies.SvelteProxy")
const JavaProxy = Java.type("net.razshare.svelte3jssr.proxies.JavaProxy")


const compiledComponents = {}

// This following instructions give Java access to some NodeJS functions and their context.
// API starts
NodeProxy.setRequire((url)=>{
    return importScripts(url)
})

SvelteProxy.setCompiler(async (source,generate)=>{   
    if(compiledComponents[source]) return compiledComponents[source]

    const bundle = await rollup({
        input:source,
        plugins:[
            svelte({
                // By default, all ".svelte" files are compiled
                //extensions: ['.my-custom-extension'],
        
                // You can restrict which files are compiled
                // using `include` and `exclude`
                include: 'src/main/svelte/**/*.svelte',
                    
                // Optionally, preprocess components with svelte.preprocess:
                // https://svelte.dev/docs#svelte_preprocess
                preprocess: {
                    style: ({ content }) => {
                        return transformStyles(content);
                    }
                },
        
                // Emit CSS as "files" for other plugins to process. default is true
                emitCss: false,
        
                // Warnings are normally passed straight to Rollup. You can
                // optionally handle them here, for example to squelch
                // warnings with a particular code
                onwarn: (warning, handler) => {
                    // e.g. don't warn on <marquee> elements, cos they're cool
                    if (warning.code === 'a11y-distracting-elements') return;
        
                    // let Rollup handle all other warnings normally
                    handler(warning);
                },
        
                // You can pass any of the Svelte compiler options
                compilerOptions: {
        
                    // By default, the client-side compiler is used. You
                    // can also use the server-side rendering compiler
                    generate,
        
                    // ensure that extra attributes are added to head
                    // elements for hydration (used with generate: 'ssr')
                    hydratable: true,
        
                    // You can optionally set 'customElement' to 'true' to compile
                    // your components to custom elements (aka web elements)
                    customElement: false
                }
            }),
            // see NOTICE below
            resolve({ browser: true }),
            // ...
        ]
    });

    const { output } = await bundle.generate({
        format:'iife'
    });
    compiledComponents[source] = output[0].code;
    return output[0].code;
})

SvelteProxy.setRender((code,generate,options)=>{
    const Component = eval(code)

    switch(generate){
        case 'dom':
            return new Component(options);
        case 'ssr':
            return Component.render(options)
    }

    return ''
})
// API ends

const app = new App()

http
.createServer((request, response)=>{
    JavaProxy.requestCallback(request, response)
})
.listen(8000, ()=>console.log("Graal.js server running at http://127.0.0.1:8000/"))