<template>
    <div>
        <div v-if="loggedIn">
            Hello, {{ name }}
        </div>
        <div v-else-if="userList == 'pending'">
            Spinner
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
import { computed, defineComponent } from 'vue'
import { useStore } from 'vuex'

export default defineComponent({
    // name: 'UserBlock',
    setup () {
        const store = useStore();

        return {
            loggedIn: computed(() => store.getters.loggedIn),
            startLogin: () => store.dispatch('startLogin'),
            userList: computed(() => store.state.userList),
            login: (user: any) => store.dispatch('login', user),
            name: computed(() => store.state.user.name),
        }
    }
})
</script>

<style scoped>

</style>