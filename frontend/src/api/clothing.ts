import { apiFetch } from './client'
import type { ClothingItemResponse, UpdateClothingItemRequest } from './types'

/** 내 옷장 (지금까지 기록에서 수동/자동으로 등록된 옷 아이템 전체). */
export function listClothingItems(): Promise<ClothingItemResponse[]> {
  return apiFetch<ClothingItemResponse[]>('/api/clothing-items')
}

export function getClothingItem(id: number): Promise<ClothingItemResponse> {
  return apiFetch<ClothingItemResponse>(`/api/clothing-items/${id}`)
}

/** 자동 인식(Gemini) 결과가 틀렸을 때 카테고리/색상/핏을 직접 고친다. */
export function updateClothingItem(
  id: number,
  request: UpdateClothingItemRequest,
): Promise<ClothingItemResponse> {
  return apiFetch<ClothingItemResponse>(`/api/clothing-items/${id}`, { method: 'PATCH', json: request })
}

export function deleteClothingItem(id: number): Promise<void> {
  return apiFetch<void>(`/api/clothing-items/${id}`, { method: 'DELETE' })
}
