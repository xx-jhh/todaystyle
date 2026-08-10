import { apiFetch } from './client'
import type { OotdResponse } from './types'

export function listOotd(): Promise<OotdResponse[]> {
  return apiFetch<OotdResponse[]>('/api/ootd')
}

export function getOotd(id: number): Promise<OotdResponse> {
  return apiFetch<OotdResponse>(`/api/ootd/${id}`)
}

/** 오늘의 착장 업로드. multipart/form-data(image + recordDate). */
export function uploadOotd(recordDate: string, image: File): Promise<OotdResponse> {
  const form = new FormData()
  form.append('recordDate', recordDate)
  form.append('image', image)
  return apiFetch<OotdResponse>('/api/ootd', { method: 'POST', body: form })
}
