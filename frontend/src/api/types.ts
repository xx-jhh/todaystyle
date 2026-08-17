// 백엔드 DTO와 1:1로 맞춘 타입. 백엔드가 필드를 바꾸면 여기도 함께 갱신한다.

export interface TokenResponse {
  accessToken: string
  tokenType: string
  expiresInMs: number
}

export type BodyType = 'STRAIGHT' | 'WAVE' | 'NATURAL'
export type StyleCategory = 'CASUAL' | 'AMEKAJI' | 'STREET' | 'MINIMAL' | 'FORMAL' | 'VINTAGE'

export interface SignUpRequest {
  email: string
  password: string
  nickname: string
  height?: number
  weight?: number
  waistInch?: number
  bodyType?: BodyType
  preferredStyle?: StyleCategory
}

export interface LoginRequest {
  email: string
  password: string
}

export interface UserResponse {
  id: number
  email: string
  nickname: string
  height: number | null
  weight: number | null
  waistInch: number | null
  bodyType: BodyType | null
  preferredStyle: StyleCategory | null
}

/** 마이페이지에서 신체 치수를 수정하는 요청. 필드를 비우면(undefined) 값이 지워진다. */
export interface UpdateBodyMeasurementsRequest {
  height?: number
  weight?: number
  waistInch?: number
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
  weather?: EntryWeather
  memo: string | null
}

export type ClothingCategory = 'TOP' | 'BOTTOM' | 'OUTER' | 'SHOES' | 'ACCESSORY' | 'DRESS'
export type Fit = 'SLIM' | 'REGULAR' | 'LOOSE' | 'OVERSIZED'

export interface ClothingItemResponse {
  id: number
  ootdRecordId: number
  recordDate: string
  category: ClothingCategory
  color: string | null
  fit: Fit | null
  imageUrl: string | null
  /** 이 아이템이 나온 OOTD 기록의 원본 착장 사진(전체 컷). 개별 아이템 크롭 사진이 없어 이걸 대신 쓴다. */
  ootdPhotoUrl: string
}

export interface CombinationResponse {
  top: ClothingItemResponse
  bottom: ClothingItemResponse
  score: number
  reason: string
}

/**
 * 타임라인 카드가 다루는 항목. 업로드 시 위치 정보를 못 받았거나 기상청 조회가
 * 실패한 기록은 weather가 없을 수 있어 선택 필드다.
 */
export interface DiaryEntry {
  id: number
  recordDate: string
  photoUrl: string
  weather?: EntryWeather
  /** 사진이 없을 때(샘플/플레이스홀더) 카드에 쓰는 그라디언트. */
  placeholderGradient?: string
  /** 상세화면에서만 쓰는 코디 메모. 홈 타임라인 카드에는 노출하지 않는다. */
  memo?: string | null
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
