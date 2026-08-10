import { Home, Plus, User } from 'lucide-react'
import { NavLink, useNavigate } from 'react-router-dom'

/** 하단 네비: 홈 / 업로드(+, 포인트컬러 강조) / 마이페이지. */
export function BottomNav() {
  const navigate = useNavigate()

  return (
    <nav className="sticky bottom-0 z-20 border-t border-line bg-paper/95 backdrop-blur-md">
      <div className="mx-auto flex h-16 max-w-[480px] items-center justify-around px-6">
        <NavLink
          to="/"
          end
          aria-label="홈"
          className={({ isActive }) =>
            `grid h-11 w-11 place-items-center rounded-xl transition-colors ${
              isActive ? 'text-accent' : 'text-ink-soft hover:text-ink'
            }`
          }
        >
          {({ isActive }) => <Home size={26} strokeWidth={isActive ? 2.4 : 1.75} />}
        </NavLink>

        {/* 업로드: 포인트 컬러 원형 버튼으로 시각적 중심 */}
        <button
          type="button"
          aria-label="업로드"
          onClick={() => navigate('/upload')}
          className="grid h-12 w-12 -translate-y-1 place-items-center rounded-full bg-accent text-white shadow-lg shadow-accent/30 transition-transform active:scale-95"
        >
          <Plus size={26} strokeWidth={2.5} />
        </button>

        <NavLink
          to="/me"
          aria-label="마이페이지"
          className={({ isActive }) =>
            `grid h-11 w-11 place-items-center rounded-xl transition-colors ${
              isActive ? 'text-accent' : 'text-ink-soft hover:text-ink'
            }`
          }
        >
          {({ isActive }) => <User size={26} strokeWidth={isActive ? 2.4 : 1.75} />}
        </NavLink>
      </div>
    </nav>
  )
}
