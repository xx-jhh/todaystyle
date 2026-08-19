import { Link } from 'react-router-dom'

/**
 * 얇은 상단 헤더바: 로고만 표시한다. 예전엔 알림/마이페이지 아이콘도 있었는데,
 * 알림은 실제 기능(백엔드 API)이 없어 눌러도 아무 반응이 없는 장식용 버튼이었고,
 * 마이페이지 아이콘은 하단 네비게이션과 완전히 중복이라 둘 다 제거했다.
 */
export function Header() {
  return (
    <header className="sticky top-0 z-20 border-b border-line bg-paper/90 backdrop-blur-md">
      <div className="flex h-14 items-center px-4">
        <Link to="/" className="flex items-baseline gap-1">
          <span className="text-lg font-extrabold tracking-tight text-ink">today</span>
          <span className="text-lg font-extrabold tracking-tight text-accent">style</span>
        </Link>
      </div>
    </header>
  )
}
