import { useEffect, useState } from 'react'
import { LogOut, Ruler, Settings, Shirt, Sparkles } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { listOotd } from '../api/ootd'
import { getCombos } from '../api/recommendations'
import { useAuth } from '../auth/AuthContext'
import { calculateStreak } from '../lib/format'

/** 추천 API가 허용하는 최대 limit(RecommendationController.MAX_LIMIT)과 맞춘 값. */
const MAX_COMBO_LIMIT = 50

export function MyPage() {
  const navigate = useNavigate()
  const { signOut } = useAuth()
  const [recordCount, setRecordCount] = useState(0)
  const [comboCount, setComboCount] = useState(0)
  const [streak, setStreak] = useState(0)

  useEffect(() => {
    let cancelled = false
    listOotd()
      .then((entries) => {
        if (!cancelled) {
          setRecordCount(entries.length)
          setStreak(calculateStreak(entries.map((e) => e.recordDate)))
        }
      })
      .catch(() => {
        // 통계는 부가 정보라 실패해도 화면 전체를 에러로 막지 않는다(0으로 유지).
      })
    getCombos(MAX_COMBO_LIMIT)
      .then((combos) => {
        if (!cancelled) setComboCount(combos.length)
      })
      .catch(() => {
        // 옷을 아직 안 올렸으면 추천이 없을 수 있어 실패해도 화면을 막지 않는다.
      })
    return () => {
      cancelled = true
    }
  }, [])

  function handleSignOut() {
    signOut()
    navigate('/login', { replace: true })
  }

  return (
    <div className="px-4 pt-6 pb-8">
      {/* 프로필 헤더 */}
      <div className="flex items-center gap-4">
        <div className="grid h-16 w-16 place-items-center rounded-full bg-accent-soft text-accent">
          <Shirt size={28} strokeWidth={1.75} />
        </div>
        <div>
          <p className="text-lg font-extrabold">나의 스타일</p>
          <p className="text-sm text-ink-soft">오늘도 기록해요</p>
        </div>
      </div>

      {/* 통계 */}
      <div className="mt-6 grid grid-cols-3 divide-x divide-line rounded-2xl border border-line bg-paper py-4 text-center">
        <div>
          <p className="text-xl font-extrabold">{recordCount}</p>
          <p className="mt-0.5 text-xs text-ink-soft">기록</p>
        </div>
        <div>
          <p className="text-xl font-extrabold">{comboCount}</p>
          <p className="mt-0.5 text-xs text-ink-soft">조합 추천</p>
        </div>
        <div>
          <p className="text-xl font-extrabold">{streak}</p>
          <p className="mt-0.5 text-xs text-ink-soft">연속일</p>
        </div>
      </div>

      {/* 메뉴 */}
      <ul className="mt-6 overflow-hidden rounded-2xl border border-line bg-paper">
        <li>
          <button
            onClick={() => navigate('/wardrobe')}
            className="flex w-full items-center gap-3 px-4 py-3.5 text-left text-sm hover:bg-canvas"
          >
            <Shirt size={18} className="text-ink-soft" /> 내 옷장
          </button>
        </li>
        <li className="border-t border-line">
          <button
            onClick={() => navigate('/me/body')}
            className="flex w-full items-center gap-3 px-4 py-3.5 text-left text-sm hover:bg-canvas"
          >
            <Ruler size={18} className="text-ink-soft" /> 신체 정보 수정
          </button>
        </li>
        <li className="border-t border-line">
          <button
            onClick={() => navigate('/recommendations')}
            className="flex w-full items-center gap-3 px-4 py-3.5 text-left text-sm hover:bg-canvas"
          >
            <Sparkles size={18} className="text-ink-soft" /> 코디 조합 추천 받기
          </button>
        </li>
        <li className="border-t border-line">
          <button className="flex w-full items-center gap-3 px-4 py-3.5 text-left text-sm hover:bg-canvas">
            <Settings size={18} className="text-ink-soft" /> 설정
          </button>
        </li>
        <li className="border-t border-line">
          <button
            onClick={handleSignOut}
            className="flex w-full items-center gap-3 px-4 py-3.5 text-left text-sm text-red-500 hover:bg-canvas"
          >
            <LogOut size={18} /> 로그아웃
          </button>
        </li>
      </ul>
    </div>
  )
}
