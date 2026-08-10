import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { signUp } from '../api/auth'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/AuthContext'

export function SignupPage() {
  const navigate = useNavigate()
  const { signIn } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [nickname, setNickname] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      // 신체 정보/체형/스타일은 이미지 선택 UI가 정해지면 추가한다(백엔드에선 선택 입력).
      const { accessToken } = await signUp({ email, password, nickname })
      signIn(accessToken)
      navigate('/', { replace: true })
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '회원가입에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="mx-auto flex min-h-dvh max-w-[480px] flex-col justify-center px-6 sm:border-x sm:border-line">
      <div className="mb-8">
        <h1 className="text-2xl font-extrabold tracking-tight">회원가입</h1>
        <p className="mt-1 text-sm text-ink-soft">todaystyle과 함께 옷 다이어리를 시작해요</p>
      </div>

      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        <Field label="이메일">
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            autoComplete="email"
            className="input"
          />
        </Field>
        <Field label="닉네임">
          <input
            type="text"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            required
            maxLength={30}
            className="input"
          />
        </Field>
        <Field label="비밀번호 (8자 이상)">
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={8}
            autoComplete="new-password"
            className="input"
          />
        </Field>
        {error && <p className="text-sm text-red-500">{error}</p>}
        <button
          type="submit"
          disabled={loading}
          className="mt-1 rounded-xl bg-accent py-3.5 font-bold text-white transition-transform active:scale-[0.99] disabled:opacity-60"
        >
          {loading ? '가입 중…' : '가입하기'}
        </button>
      </form>

      <p className="mt-6 text-center text-sm text-ink-soft">
        이미 계정이 있으신가요?{' '}
        <Link to="/login" className="font-semibold text-accent">
          로그인
        </Link>
      </p>
    </div>
  )
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <label className="flex flex-col gap-1.5">
      <span className="text-sm text-ink-soft">{label}</span>
      {children}
    </label>
  )
}
