import { useEffect, useState } from 'react'
import { Sparkles } from 'lucide-react'
import { ApiError } from '../api/client'
import { getCombos } from '../api/recommendations'
import type { CombinationResponse } from '../api/types'
import { ComboCard } from '../components/ComboCard'

export function RecommendationsPage() {
  const [combos, setCombos] = useState<CombinationResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    getCombos()
      .then((list) => {
        if (!cancelled) setCombos(list)
      })
      .catch((err) => {
        if (!cancelled) setError(err instanceof ApiError ? err.message : '추천을 불러오지 못했습니다.')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <div className="px-4 pt-5 pb-6">
      <div className="mb-5">
        <h1 className="text-2xl font-extrabold tracking-tight">코디 조합 추천</h1>
        <p className="mt-1 text-sm text-ink-soft">아직 함께 입어본 적 없는 조합이에요</p>
      </div>

      {loading && <p className="py-10 text-center text-ink-soft">불러오는 중…</p>}
      {error && <p className="py-10 text-center text-red-500">{error}</p>}

      {!loading && !error && combos.length === 0 && (
        <div className="flex flex-col items-center gap-3 py-16 text-center text-ink-soft">
          <div className="grid h-14 w-14 place-items-center rounded-full bg-accent-soft text-accent">
            <Sparkles size={26} strokeWidth={1.75} />
          </div>
          <p className="text-sm">
            추천할 조합이 아직 없어요.
            <br />
            서로 다른 날짜에 상의·하의를 각각 하나 이상 올리면
            <br />
            아직 함께 입지 않은 조합을 찾아드려요.
          </p>
        </div>
      )}

      {!loading && !error && combos.length > 0 && (
        <ul className="flex flex-col gap-3">
          {combos.map((combo) => (
            <ComboCard key={`${combo.top.id}-${combo.bottom.id}`} combo={combo} />
          ))}
        </ul>
      )}
    </div>
  )
}
