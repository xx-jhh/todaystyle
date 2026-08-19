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

function toIsoDate(d: Date): string {
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${mm}-${dd}`
}

/** 오늘 날짜를 yyyy-MM-dd(로컬 기준)로. */
export function todayIso(): string {
  return toIsoDate(new Date())
}

/**
 * 오늘(또는 어제까지) 기준으로 끊기지 않고 이어진 기록 일수.
 * 오늘 아직 안 올렸다고 바로 0으로 끊기지 않도록, 오늘 기록이 없으면 어제부터 센다.
 */
export function calculateStreak(recordDates: string[]): number {
  const dateSet = new Set(recordDates)
  const cursor = new Date()
  cursor.setHours(0, 0, 0, 0)

  if (!dateSet.has(toIsoDate(cursor))) {
    cursor.setDate(cursor.getDate() - 1)
  }

  let streak = 0
  while (dateSet.has(toIsoDate(cursor))) {
    streak += 1
    cursor.setDate(cursor.getDate() - 1)
  }
  return streak
}
