import { apiFetch } from './client'
import type { OotdResponse, PagedResponse } from './types'

export function listOotd(page = 0, size = 20): Promise<PagedResponse<OotdResponse>> {
  return apiFetch<PagedResponse<OotdResponse>>(`/api/ootd?page=${page}&size=${size}`)
}

/** 마이페이지 통계(전체 기록 수)용. 목록 API는 페이지네이션이라 전체 개수를 안 담는다. */
export function getOotdCount(): Promise<{ count: number }> {
  return apiFetch<{ count: number }>('/api/ootd/count')
}

export function getOotd(id: number): Promise<OotdResponse> {
  return apiFetch<OotdResponse>(`/api/ootd/${id}`)
}

/** 코디 메모 작성/수정. 빈 문자열을 보내면 메모가 지워진다. */
export function updateOotdMemo(id: number, memo: string): Promise<OotdResponse> {
  return apiFetch<OotdResponse>(`/api/ootd/${id}`, { method: 'PATCH', json: { memo } })
}

/** OOTD 삭제. 잘못 찍었거나 마음에 안 드는 사진을 지우고 다시 올릴 수 있게 한다. */
export function deleteOotd(id: number): Promise<void> {
  return apiFetch<void>(`/api/ootd/${id}`, { method: 'DELETE' })
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
  // Cloudinary 업로드 + 이미지 인식(Gemini)/날씨 스냅샷이 같은 요청 안에서 순차로 걸려
  // 기본 타임아웃보다 오래 걸릴 수 있다.
  return apiFetch<OotdResponse>('/api/ootd', { method: 'POST', body: form, timeoutMs: 30000 })
}
