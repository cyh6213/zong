import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_KNOWLEDGE_API_URL || '/api/knowledge',
})

export default api
