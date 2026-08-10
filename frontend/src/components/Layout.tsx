import { Outlet } from 'react-router-dom'
import { Header } from './Header'
import { BottomNav } from './BottomNav'

/**
 * 모바일 우선 셸: 최대 480px 가운데 정렬, 상단 헤더 + 하단 네비 고정.
 * 넓은 화면에서도 폰 프레임처럼 가운데에 앉는다.
 */
export function Layout() {
  return (
    <div className="mx-auto flex min-h-dvh max-w-[480px] flex-col bg-canvas shadow-sm sm:border-x sm:border-line">
      <Header />
      <main className="flex-1">
        <Outlet />
      </main>
      <BottomNav />
    </div>
  )
}
