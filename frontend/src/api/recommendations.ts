import { apiFetch } from './client'
import type { CombinationResponse } from './types'

/** 아직 입어본 적 없는 상의×하의 조합 추천 (어울림 점수 높은 순). */
export function getCombos(limit = 10): Promise<CombinationResponse[]> {
  return apiFetch<CombinationResponse[]>(`/api/recommendations/combos?limit=${limit}`)
}
