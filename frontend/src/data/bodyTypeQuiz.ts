import type { BodyType } from '../api/types'

export interface QuizOption {
  label: string
  type: BodyType
}

export interface QuizQuestion {
  question: string
  options: QuizOption[]
}

/**
 * 체형(스트레이트/웨이브/내추럴) 자가진단이 어려운 사용자를 위한 객관식 질문 세트.
 * 문항당 보기 하나가 한 체형에 대응하며, 답변을 모아 가장 많이 나온 체형을 추천한다.
 */
export const BODY_TYPE_QUIZ: QuizQuestion[] = [
  {
    question: '쇄골이 도드라져 보이나요?',
    options: [
      { label: '잘 보임', type: 'WAVE' },
      { label: '보통', type: 'NATURAL' },
      { label: '거의 안 보임', type: 'STRAIGHT' },
    ],
  },
  {
    question: '손목을 다른 손으로 감쌌을 때 느낌은?',
    options: [
      { label: '뼈가 만져지고 헐렁함', type: 'WAVE' },
      { label: '적당히 딱 맞음', type: 'STRAIGHT' },
      { label: '두꺼워서 잘 안 감김', type: 'NATURAL' },
    ],
  },
  {
    question: '어깨 라인은?',
    options: [
      { label: '각지고 탄탄함', type: 'STRAIGHT' },
      { label: '좁고 둥긂', type: 'WAVE' },
      { label: '골격이 크고 각짐', type: 'NATURAL' },
    ],
  },
  {
    question: '살이 잘 붙는 부위는?',
    options: [
      { label: '상체·복부', type: 'STRAIGHT' },
      { label: '하체·골반', type: 'WAVE' },
      { label: '잘 안 찌는 편', type: 'NATURAL' },
    ],
  },
  {
    question: '상체와 하체 중 볼륨이 더 있는 쪽은?',
    options: [
      { label: '상체', type: 'STRAIGHT' },
      { label: '하체', type: 'WAVE' },
      { label: '비슷함', type: 'NATURAL' },
    ],
  },
  {
    question: '피부·살의 느낌은?',
    options: [
      { label: '탄력 있고 단단함', type: 'STRAIGHT' },
      { label: '부드럽고 말랑함', type: 'WAVE' },
      { label: '마디가 도드라짐', type: 'NATURAL' },
    ],
  },
]

/** 답변 중 가장 많이 나온 체형을 반환한다. 동점이거나 답변이 없으면 undefined(직접 선택 유도). */
export function tallyBodyType(answers: (BodyType | undefined)[]): BodyType | undefined {
  const counts: Record<BodyType, number> = { STRAIGHT: 0, WAVE: 0, NATURAL: 0 }
  for (const answer of answers) {
    if (answer) counts[answer]++
  }
  const entries = Object.entries(counts) as [BodyType, number][]
  const max = Math.max(...entries.map(([, count]) => count))
  if (max === 0) return undefined
  const top = entries.filter(([, count]) => count === max)
  return top.length === 1 ? top[0][0] : undefined
}
