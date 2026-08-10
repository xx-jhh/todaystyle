import type { ApiErrorBody } from './types'

const TOKEN_KEY = 'todaystyle.accessToken'

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
}

/**
 * 공통 fetch 래퍼. /api 프록시(개발) 또는 동일 오리진(배포)으로 요청하고,
 * 토큰을 자동 첨부하며, 실패 시 서버 message를 담은 ApiError를 던진다.
 */
export async function apiFetch<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', json, body, auth = true } = options
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

  const res = await fetch(path, {
    method,
    headers,
    body: json !== undefined ? JSON.stringify(json) : body,
  })

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
    throw new ApiError(res.status, message)
  }

  // 204 No Content 등 본문이 없는 응답 대응
  if (res.status === 204) {
    return undefined as T
  }
  return (await res.json()) as T
}
