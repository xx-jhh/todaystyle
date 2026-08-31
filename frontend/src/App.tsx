import { Navigate, Route, Routes } from 'react-router-dom'
import { Layout } from './components/Layout'
import { ProtectedRoute } from './auth/ProtectedRoute'
import { LoginPage } from './pages/LoginPage'
import { SignupPage } from './pages/SignupPage'
import { ForgotPasswordPage } from './pages/ForgotPasswordPage'
import { ResetPasswordPage } from './pages/ResetPasswordPage'
import { HomePage } from './pages/HomePage'
import { UploadPage } from './pages/UploadPage'
import { DetailPage } from './pages/DetailPage'
import { MyPage } from './pages/MyPage'
import { RecommendationsPage } from './pages/RecommendationsPage'
import { BodyMeasurementsPage } from './pages/BodyMeasurementsPage'
import { WardrobePage } from './pages/WardrobePage'
import { ClothingItemEditPage } from './pages/ClothingItemEditPage'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />

      <Route element={<ProtectedRoute />}>
        {/* 업로드는 사진 중심 전체화면 (하단 네비 없이 몰입) */}
        <Route path="/upload" element={<UploadPage />} />

        <Route element={<Layout />}>
          <Route path="/" element={<HomePage />} />
          <Route path="/ootd/:id" element={<DetailPage />} />
          <Route path="/recommendations" element={<RecommendationsPage />} />
          <Route path="/me" element={<MyPage />} />
          <Route path="/me/body" element={<BodyMeasurementsPage />} />
          <Route path="/wardrobe" element={<WardrobePage />} />
          <Route path="/clothing-items/:id" element={<ClothingItemEditPage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
