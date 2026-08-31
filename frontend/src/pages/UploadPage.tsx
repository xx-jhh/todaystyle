import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Camera, ImagePlus, X } from 'lucide-react'
import { ApiError } from '../api/client'
import { uploadOotd } from '../api/ootd'
import { getWeather } from '../api/weather'
import type { EntryWeather } from '../api/types'
import { WeatherBadge } from '../components/WeatherBadge'
import { compressImage } from '../lib/compressImage'
import { formatLongDate, todayIso } from '../lib/format'

const FALLBACK = { lat: 37.5665, lon: 126.978 }

export function UploadPage() {
  const navigate = useNavigate()
  const fileInput = useRef<HTMLInputElement>(null)
  const [file, setFile] = useState<File | null>(null)
  const [preview, setPreview] = useState<string | null>(null)
  const [weather, setWeather] = useState<EntryWeather | null>(null)
  const [location, setLocation] = useState<{ lat: number; lon: number } | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const today = todayIso()

  // 오늘 날씨를 불러와 업로드 화면에 함께 노출 (같은 좌표를 제출 시 백엔드에도 전달)
  useEffect(() => {
    let cancelled = false
    const load = (lat: number, lon: number) => {
      setLocation({ lat, lon })
      return getWeather(lat, lon)
        .then((w) => {
          if (!cancelled) setWeather({ temp: w.currentTemp, sky: w.sky, precipitation: w.precipitation })
        })
        .catch(() => {})
    }
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => void load(pos.coords.latitude, pos.coords.longitude),
        () => void load(FALLBACK.lat, FALLBACK.lon),
        { timeout: 8000 },
      )
    } else {
      void load(FALLBACK.lat, FALLBACK.lon)
    }
    return () => {
      cancelled = true
    }
  }, [])

  // 미리보기 URL 정리
  useEffect(() => {
    return () => {
      if (preview) URL.revokeObjectURL(preview)
    }
  }, [preview])

  async function pickFile(f: File | undefined) {
    if (!f) return
    setError(null)
    // 폰 원본 사진은 수 MB라 업로드 전에 리사이즈+재인코딩해 크기를 줄인다(실패하면 원본 그대로).
    const compressed = await compressImage(f)
    if (preview) URL.revokeObjectURL(preview)
    setFile(compressed)
    setPreview(URL.createObjectURL(compressed))
  }

  async function handleSubmit() {
    if (!file) {
      fileInput.current?.click()
      return
    }
    setSubmitting(true)
    setError(null)
    try {
      const created = await uploadOotd(today, file, location ?? undefined)
      navigate(`/ootd/${created.id}`, { replace: true })
    } catch (err) {
      // Cloudinary 미설정 시 502가 날 수 있음 — 사용자에게 명확히 안내
      setError(err instanceof ApiError ? err.message : '업로드에 실패했습니다.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto flex min-h-dvh max-w-[480px] flex-col px-4 pt-4 pb-6 sm:border-x sm:border-line">
      {/* 상단: 날짜 + 닫기 */}
      <div className="mb-3 flex items-center justify-between">
        <div>
          <p className="text-xs text-ink-soft">오늘의 기록</p>
          <p className="text-base font-bold">{formatLongDate(today)}</p>
        </div>
        <button
          type="button"
          aria-label="닫기"
          onClick={() => navigate(-1)}
          className="grid h-9 w-9 place-items-center rounded-full text-ink-soft hover:bg-canvas"
        >
          <X size={22} strokeWidth={1.75} />
        </button>
      </div>

      {/* 사진 중심 영역 (4:3 세로 비율로 고정) */}
      <button
        type="button"
        onClick={() => fileInput.current?.click()}
        className="relative aspect-[3/4] w-full overflow-hidden rounded-3xl border border-line bg-ink/[0.03]"
      >
        {preview ? (
          <img src={preview} alt="선택한 착장 미리보기" className="h-full w-full object-cover" />
        ) : (
          <div className="flex h-full flex-col items-center justify-center gap-3 text-ink-soft">
            <div className="grid h-16 w-16 place-items-center rounded-full bg-accent-soft text-accent">
              <Camera size={30} strokeWidth={1.75} />
            </div>
            <p className="text-sm font-medium">오늘의 착장을 촬영하거나 선택하세요</p>
            <span className="inline-flex items-center gap-1.5 text-xs text-accent">
              <ImagePlus size={14} /> 사진 올리기
            </span>
          </div>
        )}

        {/* 날씨 배지 — 사진 위에 자연스럽게 */}
        {weather && (
          <div className="absolute top-3 right-3">
            <WeatherBadge weather={weather} />
          </div>
        )}
      </button>

      <input
        ref={fileInput}
        type="file"
        accept="image/*"
        className="hidden"
        onChange={(e) => void pickFile(e.target.files?.[0])}
      />

      {error && <p className="mt-3 text-center text-sm text-red-500">{error}</p>}

      {/* 액션 */}
      <button
        type="button"
        onClick={handleSubmit}
        disabled={submitting}
        className="mt-4 h-13 rounded-2xl bg-accent py-3.5 text-base font-bold text-white shadow-lg shadow-accent/25 transition-transform active:scale-[0.99] disabled:opacity-60"
      >
        {submitting ? '기록하는 중…' : file ? '오늘 기록 남기기' : '사진 선택하기'}
      </button>
    </div>
  )
}
