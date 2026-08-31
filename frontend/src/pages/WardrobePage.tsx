import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ChevronLeft, Shirt } from 'lucide-react'
import { listClothingItems } from '../api/clothing'
import { ApiError } from '../api/client'
import type { ClothingCategory, ClothingItemResponse } from '../api/types'
import { ClothingItemCard } from '../components/ClothingItemCard'
import { CLOTHING_CATEGORY_LABELS, CLOTHING_CATEGORY_ORDER } from '../data/clothingLabels'

export function WardrobePage() {
  const navigate = useNavigate()
  const [items, setItems] = useState<ClothingItemResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [page, setPage] = useState(0)
  const [hasNext, setHasNext] = useState(false)
  const [loadingMore, setLoadingMore] = useState(false)

  useEffect(() => {
    let cancelled = false
    listClothingItems(0)
      .then((result) => {
        if (cancelled) return
        setItems(result.items)
        setHasNext(result.hasNext)
      })
      .catch((err) => {
        if (!cancelled) setError(err instanceof ApiError ? err.message : '옷장을 불러오지 못했습니다.')
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
      const result = await listClothingItems(nextPage)
      setItems((prev) => [...prev, ...result.items])
      setHasNext(result.hasNext)
      setPage(nextPage)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '옷장을 불러오지 못했습니다.')
    } finally {
      setLoadingMore(false)
    }
  }

  const byCategory = groupByCategory(items)
  const categorySections = CLOTHING_CATEGORY_ORDER.map((category) => ({
    category,
    items: byCategory[category] ?? [],
  })).filter((section) => section.items.length > 0)

  return (
    <div>
      <div className="flex items-center gap-2 px-2 py-2">
        <button
          type="button"
          aria-label="뒤로"
          onClick={() => navigate(-1)}
          className="grid h-9 w-9 place-items-center rounded-full text-ink hover:bg-canvas"
        >
          <ChevronLeft size={24} strokeWidth={1.75} />
        </button>
        <span className="text-sm font-semibold text-ink-soft">내 옷장</span>
      </div>

      <div className="px-4 pt-3 pb-8">
        <h1 className="text-xl font-extrabold tracking-tight">내 옷장</h1>
        <p className="mt-1 text-sm text-ink-soft">
          지금까지 기록에서 인식된 옷 {items.length}
          {hasNext ? '+' : ''}개예요. 잘못 인식됐다면 탭해서 고칠 수 있어요.
        </p>

        {loading && <p className="py-16 text-center text-ink-soft">불러오는 중…</p>}
        {error && <p className="py-16 text-center text-red-500">{error}</p>}

        {!loading && !error && items.length === 0 && (
          <div className="flex flex-col items-center gap-3 py-16 text-center text-ink-soft">
            <div className="grid h-14 w-14 place-items-center rounded-full bg-accent-soft text-accent">
              <Shirt size={26} strokeWidth={1.75} />
            </div>
            <p className="text-sm">
              아직 옷장이 비어있어요.
              <br />
              OOTD를 올리면 옷이 자동으로 인식돼 여기 쌓여요.
            </p>
          </div>
        )}

        {!loading && !error && items.length > 0 && (
          <div className="mt-5 flex flex-col gap-6">
            {categorySections.map(({ category, items: categoryItems }) => (
              <section key={category}>
                <h2 className="mb-2.5 text-sm font-bold text-ink">
                  {CLOTHING_CATEGORY_LABELS[category]}
                  <span className="ml-1 font-normal text-ink-soft">{categoryItems.length}</span>
                </h2>
                <ul className="grid grid-cols-2 gap-3">
                  {categoryItems.map((item) => (
                    <ClothingItemCard key={item.id} item={item} />
                  ))}
                </ul>
              </section>
            ))}

            {hasNext && (
              <button
                type="button"
                onClick={loadMore}
                disabled={loadingMore}
                className="w-full rounded-xl border border-line py-3 text-sm font-semibold text-ink-soft hover:bg-canvas disabled:opacity-60"
              >
                {loadingMore ? '불러오는 중…' : '더 보기'}
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  )
}

function groupByCategory(
  items: ClothingItemResponse[],
): Partial<Record<ClothingCategory, ClothingItemResponse[]>> {
  const grouped: Partial<Record<ClothingCategory, ClothingItemResponse[]>> = {}
  for (const item of items) {
    ;(grouped[item.category] ??= []).push(item)
  }
  return grouped
}
