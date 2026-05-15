import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_AGENT_API_URL || '/api/agent',
})

export default api
