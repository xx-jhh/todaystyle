package com.example.todaystyle.common.storage;

/**
 * 업로드 결과. URL만으로는 나중에 실패를 되돌리기 위해 이미지를 지울 수 없어서
 * (Cloudinary의 삭제 API는 URL이 아니라 publicId를 요구한다) 함께 들고 다닌다.
 */
public record UploadedImage(String url, String publicId) {
}
