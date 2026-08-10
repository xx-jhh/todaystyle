import type { DiaryEntry } from '../api/types'
import { parseRecordDate } from '../lib/format'
import { WeatherBadge } from './WeatherBadge'

/**
 * 세로 타임라인 한 항목: 왼쪽 레일(연결선 + 노드) + 정사각 사진 카드.
 * 날씨 배지는 사진 우상단에 유리 느낌으로 얹혀 카드에 녹아든다.
 */
export function TimelineEntry({
  entry,
  onOpen,
  isLast,
}: {
  entry: DiaryEntry
  onOpen: (entry: DiaryEntry) => void
  isLast: boolean
}) {
  const { month, day, weekday, isToday } = parseRecordDate(entry.recordDate)

  return (
    <li className="relative pb-7 pl-9 last:pb-0">
      {/* 레일 연결선 (마지막 항목은 노드까지만) */}
      {!isLast && (
        <span
          aria-hidden
          className="absolute top-2 left-[10px] h-full w-px -translate-x-1/2 bg-line"
        />
      )}
      {/* 날짜 노드 */}
      <span
        aria-hidden
        className={`absolute top-1.5 left-[10px] h-3 w-3 -translate-x-1/2 rounded-full ring-4 ring-canvas ${
          isToday ? 'bg-accent' : 'bg-ink-soft/35'
        }`}
      />

      {/* 날짜 라벨 */}
      <div className="mb-2.5 flex items-center gap-2">
        <span className="text-sm font-bold text-ink">
          {month}월 {day}일
        </span>
        <span className="text-xs text-ink-soft">({weekday})</span>
        {isToday && (
          <span className="rounded-full bg-accent-soft px-2 py-0.5 text-[11px] font-semibold text-accent">
            오늘
          </span>
        )}
      </div>

      {/* 정사각 사진 카드 */}
      <button
        type="button"
        onClick={() => onOpen(entry)}
        className="group block w-full overflow-hidden rounded-2xl bg-line"
      >
        <div
          className="relative aspect-square w-full"
          style={
            entry.photoUrl
              ? undefined
              : { background: entry.placeholderGradient ?? '#e9e9ee' }
          }
        >
          {entry.photoUrl && (
            <img
              src={entry.photoUrl}
              alt={`${month}월 ${day}일의 착장`}
              loading="lazy"
              className="h-full w-full object-cover transition-transform duration-300 group-active:scale-[0.99]"
            />
          )}
          {entry.weather && (
            <div className="absolute top-3 right-3">
              <WeatherBadge weather={entry.weather} size="sm" />
            </div>
          )}
        </div>
      </button>
    </li>
  )
}
