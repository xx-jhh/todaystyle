import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { confirmPasswordReset } from '../api/auth'
import { ApiError } from '../api/client'

export function ResetPasswordPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') ?? ''
  const [newPassword, setNewPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [done, setDone] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await confirmPasswordReset({ token, newPassword })
      setDone(true)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '재설정에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="mx-auto flex min-h-dvh max-w-[480px] flex-col justify-center px-6 sm:border-x sm:border-line">
      <div className="mb-8 text-center">
        <div className="flex items-baseline justify-center gap-1">
          <span className="text-3xl font-extrabold tracking-tight">today</span>
          <span className="text-3xl font-extrabold tracking-tight text-accent">style</span>
        </div>
        <p className="mt-2 text-sm text-ink-soft">새 비밀번호를 설정하세요</p>
      </div>

      {!token ? (
        <p className="rounded-xl bg-surface-soft p-4 text-center text-sm text-ink-soft">
          유효하지 않은 링크입니다. 다시 요청해주세요.
        </p>
      ) : done ? (
        <p className="rounded-xl bg-surface-soft p-4 text-center text-sm text-ink-soft">
          비밀번호가 변경되었습니다.
        </p>
      ) : (
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <label className="flex flex-col gap-1.5">
            <span className="text-sm text-ink-soft">새 비밀번호</span>
            <input
              type="password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
              minLength={8}
              maxLength={64}
              autoComplete="new-password"
              className="input"
            />
          </label>
          {error && <p className="text-sm text-red-500">{error}</p>}
          <button
            type="submit"
            disabled={loading}
            className="mt-1 rounded-xl bg-accent py-3.5 font-bold text-white transition-transform active:scale-[0.99] disabled:opacity-60"
          >
            {loading ? '변경 중…' : '비밀번호 변경'}
          </button>
        </form>
      )}

      <p className="mt-6 text-center text-sm text-ink-soft">
        {done ? (
          <button onClick={() => navigate('/login', { replace: true })} className="font-semibold text-accent">
            로그인하러 가기
          </button>
        ) : (
          <Link to="/login" className="font-semibold text-accent">
            로그인으로 돌아가기
          </Link>
        )}
      </p>
    </div>
  )
}
