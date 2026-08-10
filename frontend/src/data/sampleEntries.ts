import type { DiaryEntry } from '../api/types'
import { todayIso } from '../lib/format'

// ⚠️ 디자인/데모용 샘플 데이터.
// 실제 OOTD 업로드(Cloudinary 연동)가 동작하면 이 파일과 홈피드의 fallback을 제거한다.
// 사진은 seed 고정된 placeholder(picsum)로, 실제 사진 다이어리처럼 보이게 한 것.

function daysAgoIso(n: number): string {
  const d = new Date(`${todayIso()}T00:00:00`)
  d.setDate(d.getDate() - n)
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${mm}-${dd}`
}

function photo(seed: string): string {
  return `https://picsum.photos/seed/${seed}/800/800`
}

export const SAMPLE_ENTRIES: DiaryEntry[] = [
  {
    id: -1,
    recordDate: daysAgoIso(0),
    photoUrl: photo('todaystyle-ootd-1'),
    weather: { temp: 27, sky: '구름많음', precipitation: '없음' },
  },
  {
    id: -2,
    recordDate: daysAgoIso(1),
    photoUrl: photo('todaystyle-ootd-2'),
    weather: { temp: 31, sky: '맑음', precipitation: '없음' },
  },
  {
    id: -3,
    recordDate: daysAgoIso(3),
    photoUrl: photo('todaystyle-ootd-3'),
    weather: { temp: 24, sky: '흐림', precipitation: '비' },
  },
  {
    id: -4,
    recordDate: daysAgoIso(5),
    photoUrl: photo('todaystyle-ootd-4'),
    weather: { temp: 29, sky: '맑음', precipitation: '없음' },
  },
  {
    id: -5,
    recordDate: daysAgoIso(8),
    photoUrl: photo('todaystyle-ootd-5'),
    weather: { temp: 22, sky: '구름많음', precipitation: '없음' },
  },
]
