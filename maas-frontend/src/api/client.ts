import axios from 'axios'

const client = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

client.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message = error.response?.data?.message || 'Request failed'
    console.error('API error:', message)
    return Promise.reject(new Error(message))
  }
)

export default client
