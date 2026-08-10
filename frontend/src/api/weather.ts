import { apiFetch } from './client'
import type { WeatherResponse } from './types'

export function getWeather(lat: number, lon: number): Promise<WeatherResponse> {
  const params = new URLSearchParams({ lat: String(lat), lon: String(lon) })
  return apiFetch<WeatherResponse>(`/api/weather?${params.toString()}`)
}
