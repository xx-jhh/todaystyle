import { apiFetch } from './client'
import type {
  LoginRequest,
  PasswordResetConfirmRequest,
  PasswordResetRequest,
  SignUpRequest,
  TokenResponse,
} from './types'

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

export function requestPasswordReset(request: PasswordResetRequest): Promise<void> {
  return apiFetch<void>('/api/auth/password-reset/request', {
    method: 'POST',
    json: request,
    auth: false,
  })
}

export function confirmPasswordReset(request: PasswordResetConfirmRequest): Promise<void> {
  return apiFetch<void>('/api/auth/password-reset/confirm', {
    method: 'POST',
    json: request,
    auth: false,
  })
}
