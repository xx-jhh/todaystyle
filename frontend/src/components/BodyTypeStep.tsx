import { useState } from 'react'
import { ChevronLeft, PersonStanding } from 'lucide-react'
import type { BodyType } from '../api/types'
import { BODY_TYPE_QUIZ, tallyBodyType } from '../data/bodyTypeQuiz'
import { BODY_TYPE_OPTIONS } from '../data/onboardingOptions'
import { SelectCard } from './SelectCard'

const RESULT_LABEL: Record<BodyType, string> = {
  STRAIGHT: '스트레이트',
  WAVE: '웨이브',
  NATURAL: '내추럴',
}

/**
 * 체형을 직접 고르기 어려운 사용자를 위한 6문항 자가진단 퀴즈.
 * 문항을 다 답하면 가장 많이 나온 체형을 카드에 미리 선택해두고, 사용자가 직접 바꿀 수도 있다.
 */
export function BodyTypeStep({
  value,
  onChange,
  onNext,
  onSkip,
}: {
  value: BodyType | undefined
  onChange: (bodyType: BodyType | undefined) => void
  onNext: () => void
  onSkip: () => void
}) {
  const [phase, setPhase] = useState<'quiz' | 'result'>('quiz')
  const [answers, setAnswers] = useState<(BodyType | undefined)[]>(
    Array(BODY_TYPE_QUIZ.length).fill(undefined),
  )
  const [qIndex, setQIndex] = useState(0)

  function selectAnswer(type: BodyType) {
    const next = [...answers]
    next[qIndex] = type
    setAnswers(next)

    if (qIndex < BODY_TYPE_QUIZ.length - 1) {
      setQIndex(qIndex + 1)
    } else {
      onChange(tallyBodyType(next))
      setPhase('result')
    }
  }

  if (phase === 'quiz') {
    const q = BODY_TYPE_QUIZ[qIndex]
    return (
      <div>
        <div className="mb-4 flex items-center gap-2">
          {qIndex > 0 && (
            <button
              type="button"
              aria-label="이전 질문"
              onClick={() => setQIndex(qIndex - 1)}
              className="grid h-7 w-7 place-items-center rounded-full text-ink-soft hover:bg-canvas"
            >
              <ChevronLeft size={18} strokeWidth={1.75} />
            </button>
          )}
          <span className="text-xs font-medium text-ink-soft">
            질문 {qIndex + 1} / {BODY_TYPE_QUIZ.length}
          </span>
        </div>
        <p className="mb-4 text-base font-bold text-ink">{q.question}</p>
        <div className="flex flex-col gap-2.5">
          {q.options.map((opt) => (
            <button
              key={opt.label}
              type="button"
              onClick={() => selectAnswer(opt.type)}
              className={`rounded-2xl border px-4 py-3.5 text-left text-sm font-medium transition-colors ${
                answers[qIndex] === opt.type
                  ? 'border-accent bg-accent-soft text-accent'
                  : 'border-line bg-paper text-ink hover:bg-canvas'
              }`}
            >
              {opt.label}
            </button>
          ))}
        </div>
        <button
          type="button"
          onClick={onSkip}
          className="mt-6 block text-center text-sm font-medium text-ink-soft hover:text-ink"
        >
          건너뛰기
        </button>
      </div>
    )
  }

  return (
    <div>
      <div className="mb-4 flex items-center gap-3 rounded-2xl bg-accent-soft px-4 py-3">
        <span className="grid h-9 w-9 shrink-0 place-items-center rounded-full bg-accent text-white">
          <PersonStanding size={18} strokeWidth={1.75} />
        </span>
        <p className="text-sm text-accent">
          {value
            ? <>답변을 종합하면 <strong>{RESULT_LABEL[value]}</strong> 체형에 가까워요</>
            : '답변이 골고루 나와서 직접 골라주세요'}
        </p>
      </div>
      <div className="flex flex-col gap-2.5">
        {BODY_TYPE_OPTIONS.map((opt) => (
          <SelectCard
            key={opt.value}
            icon={PersonStanding}
            label={opt.label}
            description={opt.description}
            selected={value === opt.value}
            onSelect={() => onChange(value === opt.value ? undefined : opt.value)}
          />
        ))}
      </div>
      <div className="mt-6 flex flex-col items-center gap-3">
        <button
          type="button"
          onClick={onNext}
          className="w-full rounded-xl bg-accent py-3.5 font-bold text-white transition-transform active:scale-[0.99]"
        >
          다음
        </button>
        <button
          type="button"
          onClick={() => {
            setAnswers(Array(BODY_TYPE_QUIZ.length).fill(undefined))
            setQIndex(0)
            setPhase('quiz')
          }}
          className="text-sm font-medium text-ink-soft hover:text-ink"
        >
          다시 진단하기
        </button>
      </div>
    </div>
  )
}
