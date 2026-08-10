import { apiFetch } from './client'
import type { LoginRequest, SignUpRequest, TokenResponse } from './types'

export function signUp(request: SignUpRequest): Promise<TokenResponse> {
  return apiFetch<TokenResponse>('/api/auth/signup', {
    method: 'POST',
    json: request,
    auth: false,
  })
}

export function login(request: LoginRequest): Promise<TokenResponse> {
  return apiFetch<TokenResponse>('/api/auth/login', {
    method: 'POST',
    json: request,
    auth: false,
  })
}
