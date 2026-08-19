import { useEffect, useState, type ReactNode } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ChevronLeft, Trash2 } from 'lucide-react'
import { ApiError } from '../api/client'
import { deleteClothingItem, getClothingItem, updateClothingItem } from '../api/clothing'
import type { ClothingCategory, ClothingItemResponse, Fit } from '../api/types'
import { CLOTHING_CATEGORY_LABELS, CLOTHING_CATEGORY_ORDER, FIT_LABELS } from '../data/clothingLabels'
import { formatLongDate } from '../lib/format'

const FIT_ORDER: Fit[] = ['SLIM', 'REGULAR', 'LOOSE', 'OVERSIZED']
const DEFAULT_COLOR = '#808080'

export function ClothingItemEditPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const itemId = Number(id)

  const [item, setItem] = useState<ClothingItemResponse | null>(null)
  const [category, setCategory] = useState<ClothingCategory>('TOP')
  const [color, setColor] = useState(DEFAULT_COLOR)
  const [fit, setFit] = useState<Fit | ''>('')

  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState<string | null>(null)
  const [saved, setSaved] = useState(false)

  const [confirmingDelete, setConfirmingDelete] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    getClothingItem(itemId)
      .then((data) => {
        if (cancelled) return
        setItem(data)
        setCategory(data.category)
        setColor(data.color ?? DEFAULT_COLOR)
        setFit(data.fit ?? '')
      })
      .catch((err) => {
        if (!cancelled) setLoadError(err instanceof ApiError ? err.message : '옷 정보를 불러오지 못했습니다.')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [itemId])

  async function handleSave() {
    setSaving(true)
    setSaveError(null)
    setSaved(false)
    try {
      const updated = await updateClothingItem(itemId, {
        category,
        color,
        fit: fit || undefined,
      })
      setItem(updated)
      setSaved(true)
    } catch (err) {
      setSaveError(err instanceof ApiError ? err.message : '저장에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  async function confirmDelete() {
    setDeleting(true)
    setDeleteError(null)
    try {
      await deleteClothingItem(itemId)
      navigate('/wardrobe', { replace: true })
    } catch (err) {
      setDeleteError(err instanceof ApiError ? err.message : '삭제에 실패했습니다.')
      setDeleting(false)
    }
  }

  return (
    <div>
      <div className="flex items-center gap-2 px-2 py-2">
        <button
          type="button"
          aria-label="뒤로"
          onClick={() => navigate(-1)}
          className="grid h-9 w-9 shrink-0 place-items-center rounded-full text-ink hover:bg-canvas"
        >
          <ChevronLeft size={24} strokeWidth={1.75} />
        </button>
        <span className="flex-1 text-sm font-semibold text-ink-soft">옷 정보 수정</span>
        {item && !confirmingDelete && (
          <button
            type="button"
            aria-label="이 옷 삭제"
            onClick={() => {
              setDeleteError(null)
              setConfirmingDelete(true)
            }}
            className="grid h-9 w-9 shrink-0 place-items-center rounded-full text-ink-soft hover:bg-red-50 hover:text-red-500"
          >
            <Trash2 size={19} strokeWidth={1.75} />
          </button>
        )}
      </div>

      {confirmingDelete && (
        <div className="mx-4 mb-2 rounded-2xl border border-red-200 bg-red-50 p-4">
          <p className="text-sm font-semibold text-red-600">이 옷을 삭제할까요?</p>
          <p className="mt-1 text-xs text-red-500">옷장 목록과 추천에서 더 이상 보이지 않아요.</p>
          {deleteError && <p className="mt-2 text-xs font-medium text-red-600">{deleteError}</p>}
          <div className="mt-3 flex justify-end gap-2">
            <button
              type="button"
              onClick={() => setConfirmingDelete(false)}
              disabled={deleting}
              className="rounded-lg px-3 py-1.5 text-sm font-medium text-ink-soft hover:bg-white disabled:opacity-60"
            >
              취소
            </button>
            <button
              type="button"
              onClick={confirmDelete}
              disabled={deleting}
              className="rounded-lg bg-red-500 px-3 py-1.5 text-sm font-semibold text-white disabled:opacity-60"
            >
              {deleting ? '삭제하는 중…' : '삭제하기'}
            </button>
          </div>
        </div>
      )}

      {loading && <p className="py-16 text-center text-ink-soft">불러오는 중…</p>}
      {loadError && <p className="py-16 text-center text-red-500">{loadError}</p>}

      {item && (
        <div className="px-4 pt-3 pb-8">
          <div className="aspect-square w-full overflow-hidden rounded-2xl bg-line">
            <img
              src={item.imageUrl ?? item.ootdPhotoUrl}
              alt="옷 사진"
              className="h-full w-full object-cover"
            />
          </div>
          <p className="mt-2 text-xs text-ink-soft">{formatLongDate(item.recordDate)} 기록에서 인식됨</p>

          <div className="mt-5 flex flex-col gap-4">
            <Field label="카테고리">
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value as ClothingCategory)}
                className="input"
              >
                {CLOTHING_CATEGORY_ORDER.map((c) => (
                  <option key={c} value={c}>
                    {CLOTHING_CATEGORY_LABELS[c]}
                  </option>
                ))}
              </select>
            </Field>

            <Field label="핏">
              <select
                value={fit}
                onChange={(e) => setFit(e.target.value as Fit | '')}
                className="input"
              >
                <option value="">선택 안 함</option>
                {FIT_ORDER.map((f) => (
                  <option key={f} value={f}>
                    {FIT_LABELS[f]}
                  </option>
                ))}
              </select>
            </Field>

            <Field label="색상">
              <div className="flex items-center gap-3">
                <input
                  type="color"
                  value={color}
                  onChange={(e) => setColor(e.target.value)}
                  className="h-11 w-14 shrink-0 cursor-pointer rounded-lg border border-line bg-white p-1"
                />
                <span className="text-sm text-ink-soft">{color.toUpperCase()}</span>
              </div>
            </Field>

            {saveError && <p className="text-sm text-red-500">{saveError}</p>}
            {saved && !saveError && <p className="text-sm text-accent">저장했어요.</p>}

            <button
              type="button"
              onClick={handleSave}
              disabled={saving}
              className="mt-2 w-full rounded-xl bg-accent py-3.5 font-bold text-white transition-transform active:scale-[0.99] disabled:opacity-60"
            >
              {saving ? '저장 중…' : '저장'}
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label className="flex flex-col gap-1.5">
      <span className="text-sm text-ink-soft">{label}</span>
      {children}
    </label>
  )
}
