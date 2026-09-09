import { apiFetch } from './client'
import type {
  ClothingItemResponse,
  CreateClothingItemRequest,
  PagedResponse,
  UpdateClothingItemRequest,
} from './types'

/** 내 옷장 (지금까지 기록에서 수동/자동으로 등록된 옷 아이템, 페이지네이션). */
export function listClothingItems(page = 0, size = 20): Promise<PagedResponse<ClothingItemResponse>> {
  return apiFetch<PagedResponse<ClothingItemResponse>>(`/api/clothing-items?page=${page}&size=${size}`)
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

/** 특정 OOTD 기록에 이미 태깅된 옷 아이템들 (자동 인식됐거나 수동으로 추가한 것 전부). */
export function listOotdItems(ootdId: number): Promise<ClothingItemResponse[]> {
  return apiFetch<ClothingItemResponse[]>(`/api/ootd/${ootdId}/items`)
}

/** 자동 인식이 안 됐을 때(또는 결과가 부족할 때) OOTD 기록에 옷 아이템을 직접 추가한다. */
export function createOotdItem(
  ootdId: number,
  request: CreateClothingItemRequest,
): Promise<ClothingItemResponse> {
  return apiFetch<ClothingItemResponse>(`/api/ootd/${ootdId}/items`, { method: 'POST', json: request })
}
