import { apiFetch } from './client'
import type { OotdResponse } from './types'

export function listOotd(): Promise<OotdResponse[]> {
  return apiFetch<OotdResponse[]>('/api/ootd')
}

export function getOotd(id: number): Promise<OotdResponse> {
  return apiFetch<OotdResponse>(`/api/ootd/${id}`)
}

/** 코디 메모 작성/수정. 빈 문자열을 보내면 메모가 지워진다. */
export function updateOotdMemo(id: number, memo: string): Promise<OotdResponse> {
  return apiFetch<OotdResponse>(`/api/ootd/${id}`, { method: 'PATCH', json: { memo } })
}

/**
 * 오늘의 착장 업로드. multipart/form-data(image + recordDate).
 * lat/lon을 함께 보내면 백엔드가 업로드 시점 날씨를 스냅샷으로 저장한다(선택).
 */
export function uploadOotd(
  recordDate: string,
  image: File,
  location?: { lat: number; lon: number },
): Promise<OotdResponse> {
  const form = new FormData()
  form.append('recordDate', recordDate)
  form.append('image', image)
  if (location) {
    form.append('lat', String(location.lat))
    form.append('lon', String(location.lon))
  }
  return apiFetch<OotdResponse>('/api/ootd', { method: 'POST', body: form })
}
