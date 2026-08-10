import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { listOotd } from '../api/ootd'
import type { DiaryEntry } from '../api/types'
import { SAMPLE_ENTRIES } from '../data/sampleEntries'
import { TimelineEntry } from '../components/TimelineEntry'

export function HomePage() {
  const navigate = useNavigate()
  const [entries, setEntries] = useState<DiaryEntry[]>([])
  const [usingSample, setUsingSample] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    listOotd()
      .then((list) => {
        if (cancelled) return
        if (list.length === 0) {
          // 아직 실제 업로드가 없으면 디자인/데모용 샘플로 타임라인을 보여준다.
          setEntries(SAMPLE_ENTRIES)
          setUsingSample(true)
        } else {
          setEntries(
            list.map((o) => ({
              id: o.id,
              recordDate: o.recordDate,
              photoUrl: o.photoUrl,
            })),
          )
        }
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

  function openDetail(entry: DiaryEntry) {
    if (entry.id < 0) return // 샘플 항목은 상세 미연결
    navigate(`/ootd/${entry.id}`)
  }

  return (
    <div className="px-4 pt-5 pb-6">
      <div className="mb-5">
        <h1 className="text-2xl font-extrabold tracking-tight">나의 옷 다이어리</h1>
        <p className="mt-1 text-sm text-ink-soft">날짜별로 쌓이는 오늘의 착장</p>
      </div>

      {loading && <p className="py-10 text-center text-ink-soft">불러오는 중…</p>}
      {error && <p className="py-10 text-center text-red-500">{error}</p>}

      {!loading && !error && (
        <>
          {usingSample && (
            <div className="mb-4 rounded-xl bg-accent-soft px-3 py-2 text-xs text-accent">
              아직 업로드한 기록이 없어 예시로 보여주고 있어요. 오른쪽 아래 + 로 첫 기록을 남겨보세요.
            </div>
          )}
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
        </>
      )}
    </div>
  )
}
