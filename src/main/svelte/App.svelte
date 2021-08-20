<script>
import { onMount } from "svelte";

export let name = "default";
onMount(()=>{
    console.log("ready!");
})
</script>

<h1>hello {name}!</h1>