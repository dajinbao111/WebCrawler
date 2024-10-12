import axios, {
  type AxiosInstance,
  type AxiosResponse,
  type InternalAxiosRequestConfig
} from 'axios'

const request: AxiosInstance = axios.create({
  baseURL: 'http://127.0.0.1:8080/',
  timeout: 5000
})

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    return config
  },
  (error: any) => {
    // 处理请求错误
    console.error(error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    if (response.status == 200) {
      return Promise.resolve(response)
    } else {
      return Promise.reject(response)
    }
  },
  (error: any) => {
    const status = error.response.status
    switch (status) {
      case 500:
        break
      case 404:
        break
      default:
    }
    return Promise.reject(error.response)
  }
)

export default request
