import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ChevronLeft, Sparkles } from 'lucide-react'
import { ApiError } from '../api/client'
import { getOotd } from '../api/ootd'
import type { DiaryEntry } from '../api/types'
import { WeatherBadge } from '../components/WeatherBadge'
import { formatLongDate } from '../lib/format'

export function DetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [entry, setEntry] = useState<DiaryEntry | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    getOotd(Number(id))
      .then((o) => {
        if (!cancelled) setEntry({ id: o.id, recordDate: o.recordDate, photoUrl: o.photoUrl })
      })
      .catch((err) => {
        if (!cancelled) setError(err instanceof ApiError ? err.message : '기록을 불러오지 못했습니다.')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [id])

  return (
    <div>
      {/* 서브 헤더: 뒤로가기 */}
      <div className="flex items-center gap-2 px-2 py-2">
        <button
          type="button"
          aria-label="뒤로"
          onClick={() => navigate(-1)}
          className="grid h-9 w-9 place-items-center rounded-full text-ink hover:bg-canvas"
        >
          <ChevronLeft size={24} strokeWidth={1.75} />
        </button>
        <span className="text-sm font-semibold text-ink-soft">기록 상세</span>
      </div>

      {loading && <p className="py-16 text-center text-ink-soft">불러오는 중…</p>}
      {error && <p className="py-16 text-center text-red-500">{error}</p>}

      {entry && (
        <article>
          {/* 큰 사진 */}
          <div className="relative aspect-square w-full bg-line">
            {entry.photoUrl && (
              <img src={entry.photoUrl} alt="착장 사진" className="h-full w-full object-cover" />
            )}
            {entry.weather && (
              <div className="absolute top-4 right-4">
                <WeatherBadge weather={entry.weather} />
              </div>
            )}
          </div>

          {/* 정보: 사진 아래 */}
          <div className="px-4 py-5">
            <h1 className="text-xl font-extrabold tracking-tight">
              {formatLongDate(entry.recordDate)}
            </h1>

            <div className="mt-4 rounded-2xl border border-line bg-paper p-4">
              <div className="flex items-center gap-2 text-sm font-semibold text-accent">
                <Sparkles size={16} /> 코디 메모
              </div>
              <p className="mt-2 text-sm leading-relaxed text-ink-soft">
                이 착장에 대한 메모와 코디 팁이 여기에 표시됩니다. (곧 추가될 기능)
              </p>
            </div>
          </div>
        </article>
      )}
    </div>
  )
}
