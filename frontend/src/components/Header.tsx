import { Bell, User } from 'lucide-react'
import { Link } from 'react-router-dom'

/** 얇은 상단 헤더바: 로고 + 알림/프로필 라인 아이콘. */
export function Header() {
  return (
    <header className="sticky top-0 z-20 border-b border-line bg-paper/90 backdrop-blur-md">
      <div className="flex h-14 items-center justify-between px-4">
        <Link to="/" className="flex items-baseline gap-1">
          <span className="text-lg font-extrabold tracking-tight text-ink">today</span>
          <span className="text-lg font-extrabold tracking-tight text-accent">style</span>
        </Link>
        <div className="flex items-center gap-1 text-ink">
          <button
            type="button"
            aria-label="알림"
            className="grid h-9 w-9 place-items-center rounded-full transition-colors hover:bg-canvas"
          >
            <Bell size={22} strokeWidth={1.75} />
          </button>
          <Link
            to="/me"
            aria-label="마이페이지"
            className="grid h-9 w-9 place-items-center rounded-full transition-colors hover:bg-canvas"
          >
            <User size={22} strokeWidth={1.75} />
          </Link>
        </div>
      </div>
    </header>
  )
}
