import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Camera } from 'lucide-react'
import { ApiError } from '../api/client'
import { listOotd } from '../api/ootd'
import { getWeather } from '../api/weather'
import type { DiaryEntry, OotdResponse, WeatherResponse } from '../api/types'
import { TimelineEntry } from '../components/TimelineEntry'
import { WeatherTipCard } from '../components/WeatherTipCard'

function toDiaryEntry(o: OotdResponse): DiaryEntry {
  return { id: o.id, recordDate: o.recordDate, photoUrl: o.photoUrl, weather: o.weather }
}

/** 위치 접근이 안 되거나 실패하면 서울 좌표로 대체(UploadPage와 동일한 fallback). */
const FALLBACK_LOCATION = { lat: 37.5665, lon: 126.978 }

export function HomePage() {
  const navigate = useNavigate()
  const [entries, setEntries] = useState<DiaryEntry[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [weather, setWeather] = useState<WeatherResponse | null>(null)
  const [page, setPage] = useState(0)
  const [hasNext, setHasNext] = useState(false)
  const [loadingMore, setLoadingMore] = useState(false)

  // 오늘의 날씨 + 코디 팁. 실패해도(위치 거부, 서비스키 미설정 등) 화면 전체를 막지 않고
  // 카드만 조용히 숨긴다 — 다른 날씨 관련 기능들과 동일한 best-effort 원칙.
  useEffect(() => {
    let cancelled = false
    const load = (lat: number, lon: number) =>
      getWeather(lat, lon)
        .then((w) => {
          if (!cancelled) setWeather(w)
        })
        .catch(() => {})
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => void load(pos.coords.latitude, pos.coords.longitude),
        () => void load(FALLBACK_LOCATION.lat, FALLBACK_LOCATION.lon),
        { timeout: 8000 },
      )
    } else {
      void load(FALLBACK_LOCATION.lat, FALLBACK_LOCATION.lon)
    }
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    let cancelled = false
    listOotd(0)
      .then((result) => {
        if (cancelled) return
        setEntries(result.items.map(toDiaryEntry))
        setHasNext(result.hasNext)
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
  }, [])

  async function loadMore() {
    const nextPage = page + 1
    setLoadingMore(true)
    try {
      const result = await listOotd(nextPage)
      setEntries((prev) => [...prev, ...result.items.map(toDiaryEntry)])
      setHasNext(result.hasNext)
      setPage(nextPage)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '기록을 불러오지 못했습니다.')
    } finally {
      setLoadingMore(false)
    }
  }

  function openDetail(entry: DiaryEntry) {
    navigate(`/ootd/${entry.id}`)
  }

  return (
    <div className="px-4 pt-5 pb-6">
      <div className="mb-5">
        <h1 className="bg-gradient-to-r from-ink to-accent bg-clip-text text-2xl font-extrabold tracking-tight text-transparent">
          오늘, 뭐 입었지?
        </h1>
        <p className="mt-1 text-sm text-ink-soft">날짜별로 쌓이는 나만의 착장 기록</p>
      </div>

      {weather && <WeatherTipCard weather={weather} />}

      {loading && <p className="py-10 text-center text-ink-soft">불러오는 중…</p>}
      {error && <p className="py-10 text-center text-red-500">{error}</p>}

      {!loading && !error && entries.length === 0 && (
        <div className="flex flex-col items-center gap-3 py-16 text-center text-ink-soft">
          <div className="grid h-14 w-14 place-items-center rounded-full bg-accent-soft text-accent">
            <Camera size={26} strokeWidth={1.75} />
          </div>
          <p className="text-sm">
            아직 기록이 없어요.
            <br />
            오른쪽 아래 + 로 오늘의 착장을 남겨보세요.
          </p>
        </div>
      )}

      {!loading && !error && entries.length > 0 && (
        <>
          <ol className="relative">
            {entries.map((entry, i) => (
              <TimelineEntry
                key={entry.id}
                entry={entry}
                onOpen={openDetail}
                isLast={i === entries.length - 1}
              />
            ))}
          </ol>
          {hasNext && (
            <button
              type="button"
              onClick={loadMore}
              disabled={loadingMore}
              className="mt-2 w-full rounded-xl border border-line py-3 text-sm font-semibold text-ink-soft hover:bg-canvas disabled:opacity-60"
            >
              {loadingMore ? '불러오는 중…' : '더 보기'}
            </button>
          )}
        </>
      )}
    </div>
  )
}
