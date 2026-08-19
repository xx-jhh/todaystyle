import { Cloud, CloudRain, CloudSnow, CloudSun, Sun } from 'lucide-react'
import type { EntryWeather } from '../api/types'

/** 하늘상태/강수 문자열 → 라인 아이콘. 백엔드 WeatherCodes 라벨과 맞춘다. */
export function pickWeatherIcon(sky: string, precipitation?: string) {
  if (precipitation && precipitation.includes('눈')) return CloudSnow
  if (precipitation && (precipitation.includes('비') || precipitation.includes('소나기')))
    return CloudRain
  if (sky.includes('흐림')) return Cloud
  if (sky.includes('구름')) return CloudSun
  return Sun
}

/**
 * 사진 카드에 얹는 날씨 배지(아이콘 + 온도). 반투명 유리 느낌으로 사진 위에 자연스럽게 녹아든다.
 */
export function WeatherBadge({
  weather,
  size = 'md',
}: {
  weather: EntryWeather
  size?: 'sm' | 'md'
}) {
  const Icon = pickWeatherIcon(weather.sky, weather.precipitation)
  const compact = size === 'sm'
  return (
    <div
      className={`inline-flex items-center gap-1.5 rounded-full bg-black/45 text-white backdrop-blur-md ${
        compact ? 'px-2.5 py-1 text-xs' : 'px-3 py-1.5 text-sm'
      }`}
    >
      <Icon size={compact ? 14 : 16} strokeWidth={2} />
      <span className="font-semibold">{Math.round(weather.temp)}°</span>
      {!compact && <span className="text-white/80">{weather.sky}</span>}
    </div>
  )
}
