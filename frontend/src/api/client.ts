import type { ApiErrorBody } from './types'

const TOKEN_KEY = 'todaystyle.accessToken'

/** 인증이 필요한 요청이 401을 받으면 발생. AuthProvider가 구독해 자동 로그아웃 처리. */
export const UNAUTHORIZED_EVENT = 'todaystyle:unauthorized'

/** 기본 요청 타임아웃(ms). 서버/네트워크가 멈춰도 화면이 무한 로딩에 갇히지 않게 한다. */
const DEFAULT_TIMEOUT_MS = 15000

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

/** 서버 에러 응답(message)을 담아 UI에서 그대로 보여줄 수 있는 에러. */
export class ApiError extends Error {
  status: number
  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

interface RequestOptions {
  method?: string
  /** JSON 본문. FormData를 보낼 때는 body를 직접 넘긴다. */
  json?: unknown
  body?: BodyInit
  /** 인증 토큰 첨부 여부. 기본 true. */
  auth?: boolean
  /** 요청 타임아웃(ms). 기본 DEFAULT_TIMEOUT_MS. 이미지 업로드처럼 오래 걸리는 요청은 늘려서 넘긴다. */
  timeoutMs?: number
}

/**
 * 공통 fetch 래퍼. /api 프록시(개발) 또는 동일 오리진(배포)으로 요청하고,
 * 토큰을 자동 첨부하며, 실패 시 서버 message를 담은 ApiError를 던진다.
 */
export async function apiFetch<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', json, body, auth = true, timeoutMs = DEFAULT_TIMEOUT_MS } = options
  const headers: Record<string, string> = {}

  if (json !== undefined) {
    headers['Content-Type'] = 'application/json'
  }
  if (auth) {
    const token = getToken()
    if (token) {
      headers['Authorization'] = `Bearer ${token}`
    }
  }

  const controller = new AbortController()
  const timeoutId = setTimeout(() => controller.abort(), timeoutMs)

  let res: Response
  try {
    res = await fetch(path, {
      method,
      headers,
      body: json !== undefined ? JSON.stringify(json) : body,
      signal: controller.signal,
    })
  } catch (err) {
    if (err instanceof DOMException && err.name === 'AbortError') {
      throw new ApiError(0, '요청 시간이 초과되었습니다. 네트워크 상태를 확인하고 다시 시도해주세요.')
    }
    throw err
  } finally {
    clearTimeout(timeoutId)
  }

  if (!res.ok) {
    let message = `요청 실패 (HTTP ${res.status})`
    try {
      const errBody = (await res.json()) as ApiErrorBody
      if (errBody?.message) {
        message = errBody.message
      }
    } catch {
      // 본문이 비었거나 JSON이 아니면 기본 메시지 유지
    }
    // 토큰을 첨부해 보낸 요청이 401이면 세션이 만료/무효한 것 — 자동 로그아웃 후 로그인 화면으로 보낸다.
    // auth: false로 보낸 로그인/회원가입 요청의 401(잘못된 비밀번호 등)은 대상이 아니다.
    if (auth && res.status === 401) {
      clearToken()
      window.dispatchEvent(new Event(UNAUTHORIZED_EVENT))
    }
    throw new ApiError(res.status, message)
  }

  // 204 No Content 등 본문이 없는 응답 대응
  if (res.status === 204) {
    return undefined as T
  }
  return (await res.json()) as T
}
