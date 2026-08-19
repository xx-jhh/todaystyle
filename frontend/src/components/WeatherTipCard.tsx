import type { WeatherResponse } from '../api/types'
import { pickWeatherIcon } from './WeatherBadge'

/**
 * 홈 화면 맨 위에 얹는 "오늘의 날씨 + 코디 팁" 카드. outfitTip은 백엔드(OutfitTip)가
 * 기온 구간별로 계산해서 내려주는 값을 그대로 보여준다.
 */
export function WeatherTipCard({ weather }: { weather: WeatherResponse }) {
  const Icon = pickWeatherIcon(weather.sky, weather.precipitation)

  return (
    <div className="mb-5 rounded-2xl border border-line bg-paper p-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="grid h-11 w-11 shrink-0 place-items-center rounded-full bg-accent-soft text-accent">
            <Icon size={22} strokeWidth={1.75} />
          </div>
          <div>
            <p className="text-sm font-bold">오늘의 날씨</p>
            <p className="text-xs text-ink-soft">
              {weather.sky}
              {weather.minTemp != null && weather.maxTemp != null &&
                ` · ${Math.round(weather.minTemp)}° / ${Math.round(weather.maxTemp)}°`}
              {!!weather.precipProbability && weather.precipProbability > 0 &&
                ` · 강수 ${weather.precipProbability}%`}
            </p>
          </div>
        </div>
        <p className="shrink-0 text-2xl font-extrabold tracking-tight">
          {Math.round(weather.currentTemp)}°
        </p>
      </div>
      <p className="mt-3 rounded-xl bg-canvas px-3 py-2.5 text-sm leading-relaxed text-ink">
        {weather.outfitTip}
      </p>
    </div>
  )
}
