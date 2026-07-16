import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import AppSelect from './components/AppSelect.vue'
import Vue3Toastify from 'vue3-toastify'
import 'vue3-toastify/dist/index.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(Vue3Toastify, {
    autoClose: 3000,       // ms — đổi số này để thay timeout
    position: 'top-right',
    theme: 'light',
})
app.component('AppSelect', AppSelect)
app.mount('#app')
