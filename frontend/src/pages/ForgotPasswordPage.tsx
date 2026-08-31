import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { requestPasswordReset } from '../api/auth'
import { ApiError } from '../api/client'

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [sent, setSent] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      await requestPasswordReset({ email })
      // 계정 존재 여부와 무관하게 항상 같은 안내를 보여준다 (서버 응답도 동일).
      setSent(true)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '요청에 실패했습니다. 잠시 후 다시 시도해주세요.')
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
        <p className="mt-2 text-sm text-ink-soft">비밀번호를 재설정할 이메일을 입력하세요</p>
      </div>

      {sent ? (
        <p className="rounded-xl bg-surface-soft p-4 text-center text-sm text-ink-soft">
          입력하신 이메일이 가입되어 있다면, 재설정 링크를 보내드렸어요.
          <br />
          받은 편지함(스팸함 포함)을 확인해주세요.
        </p>
      ) : (
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <label className="flex flex-col gap-1.5">
            <span className="text-sm text-ink-soft">이메일</span>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoComplete="email"
              className="input"
            />
          </label>
          {error && <p className="text-sm text-red-500">{error}</p>}
          <button
            type="submit"
            disabled={loading}
            className="mt-1 rounded-xl bg-accent py-3.5 font-bold text-white transition-transform active:scale-[0.99] disabled:opacity-60"
          >
            {loading ? '전송 중…' : '재설정 링크 받기'}
          </button>
        </form>
      )}

      <p className="mt-6 text-center text-sm text-ink-soft">
        <Link to="/login" className="font-semibold text-accent">
          로그인으로 돌아가기
        </Link>
      </p>
    </div>
  )
}
