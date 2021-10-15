<template>
  <div class="forces">
    <div v-if="loggedIn">
    <table>
      <thead>
        <tr><th>Name</th><th>Faction</th><th colspan="2"></th></tr>
      </thead>
      <tbody>
      <tr v-for="force in forceList" :key="force.id">
        <td>{{ force.name }}</td>
        <td>{{ force.faction }}</td>
        <td><button @click="openEdit(force)">Edit</button></td>
        <td><button @click="doDelete(force)">Delete</button></td>
      </tr>
      </tbody>
    </table>
    <div><button @click="openAdd">Add</button></div>
    <form v-if="showForm" @submit.prevent="saveForce">
      <div><label for="editName">Name</label><input id="editName" v-model="forceModel.name"></div>
      <div><label for="editFaction">Faction</label><input id="editFaction" v-model="forceModel.faction"></div>
      <div><input type="submit" value="Save"></div>
    </form>
    </div>
    <div v-else>
      Log in to see forces.
    </div>
  </div>
</template>

<script lang="ts">
import { computed, defineComponent, ref, watchEffect } from 'vue'
import { useStore } from 'vuex'
import { Force } from '../store/index'

export default defineComponent({
    setup () {
        const store = useStore()
        const forceModel = ref({name:'',faction:''})
        const showForm = ref(false)

        watchEffect(() => {
          if (store.getters.loggedIn) {
            store.dispatch('getForces')
          }
        })

        return {
            loggedIn: computed(() => store.getters.loggedIn),
            forceList: computed(() => store.state.forceList),
            forceModel,
            showForm,
            openAdd: () => {
              showForm.value = true
              forceModel.value = {name:'',faction:''}
            },
            openEdit: (force: Force) => {
              showForm.value = true
              forceModel.value = force
            },
            doDelete: (force: Force) => {
              store.dispatch('deleteForce', force)
            },
            saveForce: () => {
              showForm.value = false
              store.dispatch('saveForce', forceModel)
            }
        }
    }
})
</script>