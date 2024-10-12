export interface RestResult<T = any> {
  code: number
  message: string
  data: T
}
