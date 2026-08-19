import type { ClothingCategory, Fit } from '../api/types'

export const CLOTHING_CATEGORY_LABELS: Record<ClothingCategory, string> = {
  TOP: '상의',
  BOTTOM: '하의',
  OUTER: '아우터',
  SHOES: '신발',
  ACCESSORY: '액세서리',
  DRESS: '원피스',
}

/** 옷장 화면에서 카테고리를 보여주는 순서. */
export const CLOTHING_CATEGORY_ORDER: ClothingCategory[] = [
  'TOP',
  'BOTTOM',
  'OUTER',
  'DRESS',
  'SHOES',
  'ACCESSORY',
]

export const FIT_LABELS: Record<Fit, string> = {
  SLIM: '슬림',
  REGULAR: '레귤러',
  LOOSE: '루즈',
  OVERSIZED: '오버사이즈',
}
