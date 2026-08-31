import { useState, type FormEvent, type ReactNode } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ChevronLeft, Shirt } from 'lucide-react'
import { signUp } from '../api/auth'
import { ApiError } from '../api/client'
import type { BodyType, StyleCategory } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { BodyTypeStep } from '../components/BodyTypeStep'
import { SelectCard } from '../components/SelectCard'
import { STYLE_CATEGORY_OPTIONS } from '../data/onboardingOptions'

const STEPS = ['account', 'bodytype', 'style'] as const
type Step = (typeof STEPS)[number]

export function SignupPage() {
  const navigate = useNavigate()
  const { signIn } = useAuth()

  const [stepIndex, setStepIndex] = useState(0)
  const step: Step = STEPS[stepIndex]

  // 계정 정보 (필수)
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [nickname, setNickname] = useState('')

  // 체형 / 선호 스타일 (전부 선택 입력)
  const [bodyType, setBodyType] = useState<BodyType | undefined>()
  const [preferredStyles, setPreferredStyles] = useState<StyleCategory[]>([])

  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  function goNext() {
    setError(null)
    setStepIndex((i) => Math.min(i + 1, STEPS.length - 1))
  }

  function goBack() {
    setError(null)
    setStepIndex((i) => Math.max(i - 1, 0))
  }

  async function submit() {
    setError(null)
    setLoading(true)
    try {
      const { accessToken } = await signUp({
        email,
        password,
        nickname,
        bodyType,
        preferredStyles,
      })
      signIn(accessToken)
      navigate('/', { replace: true })
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '회원가입에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  function handleAccountSubmit(e: FormEvent) {
    e.preventDefault()
    goNext()
  }

  return (
    <div className="mx-auto flex min-h-dvh max-w-[480px] flex-col px-6 py-6 sm:border-x sm:border-line">
      {/* 상단: 뒤로가기 + 진행 표시 */}
      <div className="mb-6 flex items-center gap-3">
        {stepIndex > 0 ? (
          <button
            type="button"
            aria-label="이전"
            onClick={goBack}
            className="grid h-9 w-9 shrink-0 place-items-center rounded-full text-ink-soft hover:bg-canvas"
          >
            <ChevronLeft size={22} strokeWidth={1.75} />
          </button>
        ) : (
          <div className="h-9 w-9 shrink-0" />
        )}
        <div className="flex flex-1 gap-1.5">
          {STEPS.map((s, i) => (
            <span
              key={s}
              className={`h-1.5 flex-1 rounded-full ${i <= stepIndex ? 'bg-accent' : 'bg-line'}`}
            />
          ))}
        </div>
      </div>

      {step === 'account' && (
        <StepShell title="회원가입" subtitle="todaystyle과 함께 옷 다이어리를 시작해요">
          <form onSubmit={handleAccountSubmit} className="flex flex-col gap-4">
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
            <PrimaryButton type="submit">다음</PrimaryButton>
          </form>
          <p className="mt-6 text-center text-sm text-ink-soft">
            이미 계정이 있으신가요?{' '}
            <Link to="/login" className="font-semibold text-accent">
              로그인
            </Link>
          </p>
        </StepShell>
      )}

      {step === 'bodytype' && (
        <StepShell title="체형 진단" subtitle="체형을 잘 모르셔도 괜찮아요, 질문에 답하면 찾아드려요 (선택)">
          <BodyTypeStep value={bodyType} onChange={setBodyType} onNext={goNext} onSkip={goNext} />
        </StepShell>
      )}

      {step === 'style' && (
        <StepShell title="선호 스타일" subtitle="관심 있는 스타일을 모두 골라주세요 (선택, 복수 선택 가능)">
          <div className="flex flex-col gap-2.5">
            {STYLE_CATEGORY_OPTIONS.map((opt) => (
              <SelectCard
                key={opt.value}
                icon={Shirt}
                label={opt.label}
                description={opt.description}
                selected={preferredStyles.includes(opt.value)}
                onSelect={() =>
                  setPreferredStyles((prev) =>
                    prev.includes(opt.value)
                      ? prev.filter((style) => style !== opt.value)
                      : [...prev, opt.value],
                  )
                }
              />
            ))}
          </div>
          {error && <p className="mt-3 text-sm text-red-500">{error}</p>}
          <StepActions
            onNext={submit}
            onSkip={submit}
            nextLabel={loading ? '가입하는 중…' : '가입 완료'}
            disabled={loading}
          />
        </StepShell>
      )}
    </div>
  )
}

function StepShell({ title, subtitle, children }: { title: string; subtitle: string; children: ReactNode }) {
  return (
    <div className="flex flex-1 flex-col">
      <div className="mb-6">
        <h1 className="text-2xl font-extrabold tracking-tight">{title}</h1>
        <p className="mt-1 text-sm text-ink-soft">{subtitle}</p>
      </div>
      {children}
    </div>
  )
}

function StepActions({
  onNext,
  onSkip,
  nextLabel,
  disabled,
}: {
  onNext: () => void
  onSkip: () => void
  nextLabel: string
  disabled?: boolean
}) {
  return (
    <div className="mt-6 flex flex-col items-center gap-3">
      <PrimaryButton onClick={onNext} disabled={disabled}>
        {nextLabel}
      </PrimaryButton>
      <button
        type="button"
        onClick={onSkip}
        disabled={disabled}
        className="text-sm font-medium text-ink-soft hover:text-ink disabled:opacity-60"
      >
        건너뛰기
      </button>
    </div>
  )
}

function PrimaryButton({
  children,
  type = 'button',
  onClick,
  disabled,
}: {
  children: ReactNode
  type?: 'button' | 'submit'
  onClick?: () => void
  disabled?: boolean
}) {
  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled}
      className="w-full rounded-xl bg-accent py-3.5 font-bold text-white transition-transform active:scale-[0.99] disabled:opacity-60"
    >
      {children}
    </button>
  )
}

function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label className="flex flex-col gap-1.5">
      <span className="text-sm text-ink-soft">{label}</span>
      {children}
    </label>
  )
}
