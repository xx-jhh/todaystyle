import type { BodyType, StyleCategory } from '../api/types'

export const BODY_TYPE_OPTIONS: { value: BodyType; label: string; description: string }[] = [
  {
    value: 'STRAIGHT',
    label: '스트레이트',
    description: '상체·흉곽에 입체감과 두께가 있고 어깨가 각진 탄탄한 체형',
  },
  {
    value: 'WAVE',
    label: '웨이브',
    description: '상체는 얇고 하체(골반)에 볼륨이 집중된 부드러운 체형',
  },
  {
    value: 'NATURAL',
    label: '내추럴',
    description: '골격이 크고 관절이 도드라지며 팔다리가 긴 체형',
  },
]

export const STYLE_CATEGORY_OPTIONS: { value: StyleCategory; label: string; description: string }[] = [
  { value: 'CASUAL', label: '캐주얼', description: '편안하고 무난한 데일리룩' },
  { value: 'AMEKAJI', label: '아메카지', description: '빈티지 워크웨어 무드' },
  { value: 'STREET', label: '스트릿', description: '오버사이즈, 스트릿 무드' },
  { value: 'MINIMAL', label: '미니멀', description: '심플하고 절제된 톤' },
  { value: 'FORMAL', label: '포멀', description: '단정한 오피스·세미정장' },
  { value: 'VINTAGE', label: '빈티지', description: '레트로한 무드' },
]
