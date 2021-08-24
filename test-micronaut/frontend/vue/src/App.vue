<template>
  <div id="nav">
    <router-link to="/">Home</router-link> |
    <router-link to="/forces">Forces</router-link>
    <user-block />
  </div>
  <router-view />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import UserBlock from './components/UserBlock.vue'

export default defineComponent({
  components: { UserBlock },
  setup() {
    return {
    }
  },
})
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

#nav {
  padding: 30px;
}

#nav a {
  font-weight: bold;
  color: #2c3e50;
}

#nav a.router-link-exact-active {
  color: #42b983;
}
</style>
