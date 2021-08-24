import { createStore } from "vuex";

export interface User {
  id: string
  name: string
}

export interface Force {
  id: string
  name: string
  faction: string
}

export interface State {
  user: User | null
  userList: User[] | null
  forceList: Force[]
}

export default createStore<State>({
  state: {
    user: null,
    userList: null,
    forceList: []
  },
  getters: {
    loggedIn (state): boolean {
      return state.user != null
    }
  },
  mutations: {
    userList(state, value) {
      state.userList = value
    },
    login(state, user) {
      state.user = user
    },
    forceList(state, value) {
      state.forceList = value
    }
  },
  actions: {
    startLogin ({ commit }) {
      commit('userList', 'pending')
      fetch('/api/users')
      .then(response => response.json())
      .then(data => commit('userList', data))
    },
    login ({ commit }, user) {
      commit('login', user)
    },
    getForces(ctx) {
      if (ctx.state.user) {
        const userId = ctx.state.user!.id
        fetch(`/api/users/${userId}/forces`)
        .then(response => response.json())
        .then(data => ctx.commit('forceList', data))
      }
    },
    deleteForce(ctx, force) {
        fetch(`/api/forces/${force.id}`, {
          method: 'DELETE'
        })
        .then(() => {
          ctx.commit('forceList', ctx.state.forceList.filter(f => f.id != force.id))
        })
    },
    saveForce(ctx, forceModel) {
      const url = forceModel.value.id ? `/api/forces/${forceModel.value.id}` : `/api/forces`
      const body: {name: string, faction: string, playerId?: string} = {name: forceModel.value.name, faction: forceModel.value.faction}
      if (!forceModel.value.id) {
        body.playerId = ctx.state.user?.id
      }
        fetch(url, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(body)
        })
        .then(response => response.json())
        .then(newForce => {
          const newList = ctx.state.forceList.filter(f => f.id != forceModel.value.id)
          newList.push(newForce)
          ctx.commit('forceList', newList)
        })
      

    }
  },
  modules: {},
});
