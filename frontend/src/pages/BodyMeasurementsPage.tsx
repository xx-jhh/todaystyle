import { useEffect, useState, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import { ChevronLeft } from 'lucide-react'
import { ApiError } from '../api/client'
import { getMe, updateBodyMeasurements } from '../api/user'

const RANGES = {
  height: { min: 100, max: 250, label: '키 (cm)' },
  weight: { min: 20, max: 300, label: '몸무게 (kg)' },
  waistInch: { min: 15, max: 60, label: '허리 (인치)' },
} as const

export function BodyMeasurementsPage() {
  const navigate = useNavigate()
  const [height, setHeight] = useState('')
  const [weight, setWeight] = useState('')
  const [waistInch, setWaistInch] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [saved, setSaved] = useState(false)

  useEffect(() => {
    let cancelled = false
    getMe()
      .then((me) => {
        if (cancelled) return
        setHeight(me.height != null ? String(me.height) : '')
        setWeight(me.weight != null ? String(me.weight) : '')
        setWaistInch(me.waistInch != null ? String(me.waistInch) : '')
      })
      .catch((err) => {
        if (!cancelled) setError(err instanceof ApiError ? err.message : '정보를 불러오지 못했습니다.')
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  async function handleSave() {
    setError(null)
    setSaved(false)
    setSaving(true)
    try {
      const updated = await updateBodyMeasurements({
        height: height ? Number(height) : undefined,
        weight: weight ? Number(weight) : undefined,
        waistInch: waistInch ? Number(waistInch) : undefined,
      })
      setHeight(updated.height != null ? String(updated.height) : '')
      setWeight(updated.weight != null ? String(updated.weight) : '')
      setWaistInch(updated.waistInch != null ? String(updated.waistInch) : '')
      setSaved(true)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '저장에 실패했습니다.')
    } finally {
      setSaving(false)
    }
  }

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
        <span className="text-sm font-semibold text-ink-soft">신체 정보</span>
      </div>

      <div className="px-4 pt-3 pb-8">
        <h1 className="text-xl font-extrabold tracking-tight">키·몸무게·허리인치</h1>
        <p className="mt-1 text-sm text-ink-soft">입력한 값은 언제든 다시 수정할 수 있어요 (선택)</p>

        {loading ? (
          <p className="py-16 text-center text-ink-soft">불러오는 중…</p>
        ) : (
          <div className="mt-6 flex flex-col gap-4">
            <Field
              label={RANGES.height.label}
              hint={`${RANGES.height.min}~${RANGES.height.max}`}
            >
              <input
                type="number"
                inputMode="numeric"
                value={height}
                onChange={(e) => setHeight(e.target.value)}
                min={RANGES.height.min}
                max={RANGES.height.max}
                className="input"
              />
            </Field>
            <Field
              label={RANGES.weight.label}
              hint={`${RANGES.weight.min}~${RANGES.weight.max}`}
            >
              <input
                type="number"
                inputMode="numeric"
                value={weight}
                onChange={(e) => setWeight(e.target.value)}
                min={RANGES.weight.min}
                max={RANGES.weight.max}
                className="input"
              />
            </Field>
            <Field
              label={RANGES.waistInch.label}
              hint={`${RANGES.waistInch.min}~${RANGES.waistInch.max}`}
            >
              <input
                type="number"
                inputMode="numeric"
                value={waistInch}
                onChange={(e) => setWaistInch(e.target.value)}
                min={RANGES.waistInch.min}
                max={RANGES.waistInch.max}
                className="input"
              />
            </Field>

            {error && <p className="text-sm text-red-500">{error}</p>}
            {saved && !error && <p className="text-sm text-accent">저장했어요.</p>}

            <button
              type="button"
              onClick={handleSave}
              disabled={saving}
              className="mt-2 w-full rounded-xl bg-accent py-3.5 font-bold text-white transition-transform active:scale-[0.99] disabled:opacity-60"
            >
              {saving ? '저장 중…' : '저장'}
            </button>
          </div>
        )}
      </div>
    </div>
  )
}

function Field({ label, hint, children }: { label: string; hint: string; children: ReactNode }) {
  return (
    <label className="flex flex-col gap-1.5">
      <span className="flex items-baseline justify-between text-sm text-ink-soft">
        {label}
        <span className="text-xs text-ink-soft/70">{hint}</span>
      </span>
      {children}
    </label>
  )
}
