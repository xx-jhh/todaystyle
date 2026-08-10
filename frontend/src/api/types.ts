// 백엔드 DTO와 1:1로 맞춘 타입. 백엔드가 필드를 바꾸면 여기도 함께 갱신한다.

export interface TokenResponse {
  accessToken: string
  tokenType: string
  expiresInMs: number
}

export interface SignUpRequest {
  email: string
  password: string
  nickname: string
  height?: number
  weight?: number
  waistInch?: number
  bodyType?: string
  preferredStyle?: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface WeatherResponse {
  currentTemp: number
  minTemp: number
  maxTemp: number
  sky: string
  precipitation: string
  precipProbability: number
  outfitTip: string
}

export interface OotdResponse {
  id: number
  recordDate: string // yyyy-MM-dd
  photoUrl: string
  createdAt: string // ISO datetime
}

/**
 * 타임라인 카드가 다루는 항목. 백엔드 OOTD에는 아직 날씨가 저장되지 않아
 * weather는 선택 필드다(오늘 항목이나 향후 백엔드 확장 시 채워짐).
 */
export interface DiaryEntry {
  id: number
  recordDate: string
  photoUrl: string
  weather?: EntryWeather
  /** 사진이 없을 때(샘플/플레이스홀더) 카드에 쓰는 그라디언트. */
  placeholderGradient?: string
}

export interface EntryWeather {
  temp: number
  sky: string
  precipitation?: string
}

/** 서버가 GlobalExceptionHandler에서 내려주는 에러 응답의 대략적 형태. */
export interface ApiErrorBody {
  message?: string
  error?: string
  status?: number
}
