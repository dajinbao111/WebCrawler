import request from '@/utils/request'

interface Task {
  id: string
  taskType: string
  taskUrl: string
  taskStatus: string
  failReason: string
  createAt: string
  updatedAt: string
}

interface TaskStatus {
  status: string
  count: number
}

export function createTask(taskType: string, taskUrl: string): Promise<void> {
  return request({
    url: '/v1/tasks/create',
    method: 'post',
    params: {
      taskType: taskType,
      taskUrl: taskUrl
    }
  })
}

export function taskStatus(): Promise<TaskStatus[]> {
  return request({
    url: '/v1/tasks/status',
    method: 'get'
  })
}

export function listTask(taskStatus: string, pageNo: number, pageSize: number): Promise<Task[]> {
  return request({
    url: '/v1/tasks/list',
    method: 'get',
    params: {
      taskStatus: taskStatus,
      pageNo: pageNo,
      pageSize: pageSize
    }
  })
}
