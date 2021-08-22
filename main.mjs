import svelte from 'rollup-plugin-svelte'
import resolve from '@rollup/plugin-node-resolve'
import http from "http"
import { rollup } from 'rollup';


const Entry = Java.type("net.razshare.svelte3jssr.Entry")
const NodeProxy = Java.type("net.razshare.svelte3jssr.proxies.NodeProxy")
const SvelteProxy = Java.type("net.razshare.svelte3jssr.proxies.SvelteProxy")
const JavaProxy = Java.type("net.razshare.svelte3jssr.proxies.JavaProxy")

// FLAGS START
////////////////////////////////////////////////////
//If this is enabled the server will cache in compiled ".svelte" files, making it faster to server.
const USE_CACHE = true
//If this is enabled, the server will include props in the prerendered version of the component.
//This will make it easier to SEO.
//Make no mistake the JS bundle will still contain your data and render it properly if this flag is disabled.
const PREPRENDER_PROPS = true
////////////////////////////////////////////////////
// FLAGS END

const compiledComponents = {}

// This following instructions give Java access to some NodeJS functions and their context.
// API STARTS
////////////////////////////////////////////////////
NodeProxy.setRequire((url)=>{
    return importScripts(url)
})

NodeProxy.setJson(JSON)

SvelteProxy.setCompiler(async (source,generate)=>{
    if(USE_CACHE && compiledComponents[`${source}::${generate}`]) return compiledComponents[`${source}::${generate}`]

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
    })

    const { output } = await bundle.generate({
        format:'iife'
    })
    compiledComponents[`${source}::${generate}`] = output[0].code
    return output[0].code
})

SvelteProxy.setRender((code,props)=>{
    const Component = eval(code)
    console.log("############################################")
    console.log("code:",Component.render.toString())
    console.log("############################################")
    return Component.render(PREPRENDER_PROPS?props:{})
})
////////////////////////////////////////////////////
// API ENDS



/**
 * I'm running a node js server here but you can just initialize your 
 * own Java server inside src/main/java/Entry.java instead.
 * 
 * It will probably be hard to spin off a Spring Boot server because Spring Boot does some code transformations that are not possible in GraalVM, at least not with the default Spring Boot project setup.
 * However since then this came out: https://spring.io/blog/2020/04/16/spring-tips-the-graalvm-native-image-builder-feature
 * 
 * That being said you should be perfectly able to run a Quarkus web server using the same familiar Spring API.
 * For more information refer to: https://quarkus.io/
 */

new Entry() //initialize Java app.

http
.createServer((request, response)=>{
    JavaProxy.requestCallback(request, response)
})
.listen(8000, ()=>console.log("Graal.js server running at http://127.0.0.1:8000/"))