import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_COMMUNITY_API_URL || '/api/community',
})

export default api
