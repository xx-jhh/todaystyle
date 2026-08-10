import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from './AuthContext'

/** 인증되지 않은 접근을 로그인 화면으로 보낸다. */
export function ProtectedRoute() {
  const { isAuthenticated } = useAuth()
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />
}
