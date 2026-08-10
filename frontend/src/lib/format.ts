const WEEKDAYS = ['일', '월', '화', '수', '목', '금', '토']

/** yyyy-MM-dd → { month, day, weekday, isToday } */
export function parseRecordDate(recordDate: string) {
  const d = new Date(`${recordDate}T00:00:00`)
  const today = new Date()
  const isToday =
    d.getFullYear() === today.getFullYear() &&
    d.getMonth() === today.getMonth() &&
    d.getDate() === today.getDate()
  return {
    date: d,
    month: d.getMonth() + 1,
    day: d.getDate(),
    weekday: WEEKDAYS[d.getDay()],
    isToday,
  }
}

/** "8월 11일 (월)" 형태 라벨. */
export function formatLongDate(recordDate: string): string {
  const { month, day, weekday } = parseRecordDate(recordDate)
  return `${month}월 ${day}일 (${weekday})`
}

/** 오늘 날짜를 yyyy-MM-dd(로컬 기준)로. */
export function todayIso(): string {
  const d = new Date()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${mm}-${dd}`
}
