<script context="module">
    if(Java.type){
        /**
         * This runs only in the server console, it should be possible to pass this data to the client 
         * component from inside context="module", but for now your best way to pass in data is
         * proper Svelte syntax: "export let name" and pass in the props from the compiler.
        */

        const TestClass = Java.type("net.razshare.svelte3jssr.tools.TestClass")
        let test = new TestClass()
        console.log("Here's a message from Java:",test.getMessage())
    }
</script>
<script>
    import { onMount } from "svelte"
    import SomeComponent from "./SomeComponent.svelte"
    import test from './scripts/test.mjs'
    export let name = "default"
    onMount(()=>{
        console.log("App mounted!")
        test()
    })
</script>
<h1>hello {name}!</h1>
<SomeComponent/>