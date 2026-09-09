import { useEffect, useState, type ReactNode } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { ChevronLeft, Pencil, Plus, Shirt, Sparkles, Trash2 } from 'lucide-react'
import { ApiError } from '../api/client'
import { createOotdItem, listOotdItems } from '../api/clothing'
import { deleteOotd, getOotd, updateOotdMemo } from '../api/ootd'
import type { ClothingCategory, ClothingItemResponse, DiaryEntry, Fit } from '../api/types'
import { ClothingItemCard } from '../components/ClothingItemCard'
import { WeatherBadge } from '../components/WeatherBadge'
import { CLOTHING_CATEGORY_LABELS, CLOTHING_CATEGORY_ORDER, FIT_LABELS } from '../data/clothingLabels'
import { formatLongDate } from '../lib/format'

const FIT_ORDER: Fit[] = ['SLIM', 'REGULAR', 'LOOSE', 'OVERSIZED']
const DEFAULT_COLOR = '#808080'

export function DetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [entry, setEntry] = useState<DiaryEntry | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const [editing, setEditing] = useState(false)
  const [memoDraft, setMemoDraft] = useState('')
  const [saving, setSaving] = useState(false)
  const [memoError, setMemoError] = useState<string | null>(null)

  const [confirmingDelete, setConfirmingDelete] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)

  const [items, setItems] = useState<ClothingItemResponse[]>([])
  const [itemsLoading, setItemsLoading] = useState(true)

  const [addingItem, setAddingItem] = useState(false)
  const [newCategory, setNewCategory] = useState<ClothingCategory>('TOP')
  const [newColor, setNewColor] = useState(DEFAULT_COLOR)
  const [newFit, setNewFit] = useState<Fit | ''>('')
  const [addSaving, setAddSaving] = useState(false)
  const [addError, setAddError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    getOotd(Number(id))
      .then((o) => {
        if (!cancelled) {
          setEntry({ id: o.id, recordDate: o.recordDate, photoUrl: o.photoUrl, weather: o.weather, memo: o.memo })
        }
      })
      .catch((err) => {
        if (!cancelled) setError(err instanceof ApiError ? err.message : '기록을 불러오지 못했습니다.')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    listOotdItems(Number(id))
      .then((data) => {
        if (!cancelled) setItems(data)
      })
      .catch(() => {
        // 옷 아이템 목록은 부가 정보라 실패해도 사진/메모는 그대로 보여준다.
      })
      .finally(() => {
        if (!cancelled) setItemsLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [id])

  function startAddingItem() {
    setNewCategory('TOP')
    setNewColor(DEFAULT_COLOR)
    setNewFit('')
    setAddError(null)
    setAddingItem(true)
  }

  async function saveNewItem() {
    if (!entry) return
    setAddSaving(true)
    setAddError(null)
    try {
      const created = await createOotdItem(entry.id, {
        category: newCategory,
        color: newColor,
        fit: newFit || undefined,
      })
      setItems((prev) => [...prev, created])
      setAddingItem(false)
    } catch (err) {
      setAddError(err instanceof ApiError ? err.message : '옷 추가에 실패했습니다.')
    } finally {
      setAddSaving(false)
    }
  }

  function startEditing() {
    setMemoDraft(entry?.memo ?? '')
    setMemoError(null)
    setEditing(true)
  }

  async function saveMemo() {
    if (!entry) return
    setSaving(true)
    setMemoError(null)
    try {
      const updated = await updateOotdMemo(entry.id, memoDraft)
      setEntry({ ...entry, memo: updated.memo })
      setEditing(false)
    } catch (err) {
      setMemoError(err instanceof ApiError ? err.message : '메모 저장에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

  async function confirmDelete() {
    if (!entry) return
    setDeleting(true)
    setDeleteError(null)
    try {
      await deleteOotd(entry.id)
      navigate('/', { replace: true })
    } catch (err) {
      setDeleteError(err instanceof ApiError ? err.message : '삭제에 실패했습니다.')
      setDeleting(false)
    }
  }

  return (
    <div>
      {/* 서브 헤더: 뒤로가기 + 삭제 */}
      <div className="flex items-center gap-2 px-2 py-2">
        <button
          type="button"
          aria-label="뒤로"
          onClick={() => navigate(-1)}
          className="grid h-9 w-9 shrink-0 place-items-center rounded-full text-ink hover:bg-canvas"
        >
          <ChevronLeft size={24} strokeWidth={1.75} />
        </button>
        <span className="flex-1 text-sm font-semibold text-ink-soft">기록 상세</span>
        {entry && !confirmingDelete && (
          <button
            type="button"
            aria-label="이 기록 삭제"
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

      {/* 삭제 확인 배너: 실수로 눌러도 되돌릴 수 있게 명시적 확인/취소 버튼을 둔다. */}
      {confirmingDelete && (
        <div className="mx-4 mb-2 rounded-2xl border border-red-200 bg-red-50 p-4">
          <p className="text-sm font-semibold text-red-600">이 기록을 삭제할까요?</p>
          <p className="mt-1 text-xs text-red-500">사진과 메모가 모두 사라지고, 되돌릴 수 없어요.</p>
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
      {error && <p className="py-16 text-center text-red-500">{error}</p>}

      {entry && (
        <article>
          {/* 큰 사진 (4:3 세로 비율) */}
          <div className="relative aspect-[3/4] w-full bg-line">
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
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-sm font-semibold text-accent">
                  <Sparkles size={16} /> 코디 메모
                </div>
                {!editing && (
                  <button
                    type="button"
                    onClick={startEditing}
                    aria-label="메모 수정"
                    className="grid h-7 w-7 place-items-center rounded-full text-ink-soft hover:bg-canvas"
                  >
                    <Pencil size={14} strokeWidth={1.75} />
                  </button>
                )}
              </div>

              {editing ? (
                <div className="mt-2">
                  <textarea
                    value={memoDraft}
                    onChange={(e) => setMemoDraft(e.target.value)}
                    placeholder="이 착장에 대한 메모를 남겨보세요"
                    rows={4}
                    maxLength={2000}
                    autoFocus
                    className="input resize-none"
                  />
                  {memoError && <p className="mt-2 text-sm text-red-500">{memoError}</p>}
                  <div className="mt-2 flex justify-end gap-2">
                    <button
                      type="button"
                      onClick={() => setEditing(false)}
                      disabled={saving}
                      className="rounded-lg px-3 py-1.5 text-sm font-medium text-ink-soft hover:bg-canvas disabled:opacity-60"
                    >
                      취소
                    </button>
                    <button
                      type="button"
                      onClick={saveMemo}
                      disabled={saving}
                      className="rounded-lg bg-accent px-3 py-1.5 text-sm font-semibold text-white disabled:opacity-60"
                    >
                      {saving ? '저장 중…' : '저장'}
                    </button>
                  </div>
                </div>
              ) : (
                <p className="mt-2 whitespace-pre-wrap text-sm leading-relaxed text-ink-soft">
                  {entry.memo || '이 착장에 대한 메모를 남겨보세요.'}
                </p>
              )}
            </div>

            <div className="mt-4 rounded-2xl border border-line bg-paper p-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-sm font-semibold text-accent">
                  <Shirt size={16} /> 태깅된 옷
                  {!itemsLoading && <span className="font-normal text-ink-soft">{items.length}</span>}
                </div>
                {!addingItem && (
                  <button
                    type="button"
                    onClick={startAddingItem}
                    aria-label="옷 추가"
                    className="grid h-7 w-7 place-items-center rounded-full text-ink-soft hover:bg-canvas"
                  >
                    <Plus size={16} strokeWidth={1.75} />
                  </button>
                )}
              </div>

              {itemsLoading && <p className="mt-2 text-sm text-ink-soft">불러오는 중…</p>}

              {!itemsLoading && items.length === 0 && !addingItem && (
                <p className="mt-2 text-sm text-ink-soft">
                  아직 태깅된 옷이 없어요. 자동 인식이 실패했다면 직접 추가해보세요.
                </p>
              )}

              {items.length > 0 && (
                <ul className="mt-3 grid grid-cols-3 gap-2.5">
                  {items.map((item) => (
                    <ClothingItemCard key={item.id} item={item} />
                  ))}
                </ul>
              )}

              {addingItem && (
                <div className="mt-3 flex flex-col gap-3 border-t border-line pt-3">
                  <Field label="카테고리">
                    <select
                      value={newCategory}
                      onChange={(e) => setNewCategory(e.target.value as ClothingCategory)}
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
                      value={newFit}
                      onChange={(e) => setNewFit(e.target.value as Fit | '')}
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
                        value={newColor}
                        onChange={(e) => setNewColor(e.target.value)}
                        className="h-11 w-14 shrink-0 cursor-pointer rounded-lg border border-line bg-white p-1"
                      />
                      <span className="text-sm text-ink-soft">{newColor.toUpperCase()}</span>
                    </div>
                  </Field>

                  {addError && <p className="text-sm text-red-500">{addError}</p>}

                  <div className="flex justify-end gap-2">
                    <button
                      type="button"
                      onClick={() => setAddingItem(false)}
                      disabled={addSaving}
                      className="rounded-lg px-3 py-1.5 text-sm font-medium text-ink-soft hover:bg-canvas disabled:opacity-60"
                    >
                      취소
                    </button>
                    <button
                      type="button"
                      onClick={saveNewItem}
                      disabled={addSaving}
                      className="rounded-lg bg-accent px-3 py-1.5 text-sm font-semibold text-white disabled:opacity-60"
                    >
                      {addSaving ? '추가하는 중…' : '추가'}
                    </button>
                  </div>
                </div>
              )}
            </div>
          </div>
        </article>
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
