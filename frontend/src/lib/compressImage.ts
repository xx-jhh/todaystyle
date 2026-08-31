/** 업로드 전 리사이즈 기준. 이보다 길면 이 길이로 줄이고, 이미 작으면 손대지 않는다. */
const MAX_DIMENSION = 1600
const JPEG_QUALITY = 0.82

/**
 * 폰 카메라 원본(수 MB)을 그대로 올리면 모바일 데이터로는 느리고, Cloudinary/Gemini 호출
 * 비용도 커진다. 업로드 전 브라우저에서 리사이즈+JPEG 재인코딩해 크기를 줄인다.
 *
 * 디코딩 실패(예: 브라우저가 못 읽는 이미지 포맷) 등 어떤 이유로든 압축이 안 되면 원본
 * File을 그대로 반환한다 — 압축은 최적화일 뿐, 업로드 자체를 막아서는 안 된다.
 */
export async function compressImage(file: File): Promise<File> {
  try {
    // imageOrientation: 'from-image' — 안 주면 폰 사진의 EXIF 회전 정보를 무시해서
    // 세로로 찍은 사진이 캔버스에 눕혀진 채로 그려지는 문제가 생긴다.
    const bitmap = await createImageBitmap(file, { imageOrientation: 'from-image' })
    try {
      const scale = Math.min(1, MAX_DIMENSION / Math.max(bitmap.width, bitmap.height))
      if (scale >= 1) {
        // 이미 충분히 작음 — 화질 손실 없이 원본 그대로 사용.
        return file
      }

      const canvas = document.createElement('canvas')
      canvas.width = Math.round(bitmap.width * scale)
      canvas.height = Math.round(bitmap.height * scale)
      const ctx = canvas.getContext('2d')
      if (!ctx) return file
      ctx.drawImage(bitmap, 0, 0, canvas.width, canvas.height)

      const blob = await new Promise<Blob | null>((resolve) =>
        canvas.toBlob(resolve, 'image/jpeg', JPEG_QUALITY),
      )
      if (!blob) return file

      const name = file.name.replace(/\.\w+$/, '') + '.jpg'
      return new File([blob], name, { type: 'image/jpeg' })
    } finally {
      bitmap.close()
    }
  } catch {
    return file
  }
}
