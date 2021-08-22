<template>
    <div>
        <h1>Hello world </h1>
        <div v-if="loggedIn">
        </div>
        <div v-else-if="userList">
            <ul>
                <li v-for="user in userList" :key="user.id">
                    <span @click="login(user)">{{ user.name }}</span>
                </li>
            </ul>
        </div>
        <div v-else>
            <span @click="startLogin()">Log In</span>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { useStore } from 'vuex'

export default defineComponent({
    // name: 'UserBlock',
    setup () {
        const store = useStore();

        return {
            loggedIn: store.getters.loggedIn,
            startLogin: store.dispatch('startLogin'),
            userList: null
        }
    }
})
</script>

<style scoped>

</style>