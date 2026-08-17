import { apiFetch } from './client'
import type { UpdateBodyMeasurementsRequest, UserResponse } from './types'

export function getMe(): Promise<UserResponse> {
  return apiFetch<UserResponse>('/api/users/me')
}

/** 키/몸무게/허리인치 수정. 필드를 빼고 보내면 해당 값이 지워진다. */
export function updateBodyMeasurements(request: UpdateBodyMeasurementsRequest): Promise<UserResponse> {
  return apiFetch<UserResponse>('/api/users/me', { method: 'PATCH', json: request })
}
