import { createStore } from "vuex";

export default createStore({
  state: {
    user: null
  },
  getters: {
    loggedIn (state) {
      return state.user != null
    }
  },
  mutations: {},
  actions: {
    startLogin () {
      console.log('startLogin')
    }
  },
  modules: {},
});
