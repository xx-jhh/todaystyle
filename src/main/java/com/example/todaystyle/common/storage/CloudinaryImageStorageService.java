package com.example.todaystyle.common.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CloudinaryImageStorageService implements ImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryImageStorageService.class);

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    public CloudinaryImageStorageService(Cloudinary cloudinary, CloudinaryProperties properties) {
        this.cloudinary = cloudinary;
        this.properties = properties;
    }

    @Override
    public UploadedImage upload(byte[] imageBytes, String folder) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("이미지 파일이 비어 있습니다.");
        }
        if (!properties.isConfigured()) {
            throw new ImageUploadException("Cloudinary 자격증명이 설정되지 않았습니다. "
                    + "CLOUDINARY_CLOUD_NAME / CLOUDINARY_API_KEY / CLOUDINARY_API_SECRET 를 설정하세요.", null);
        }
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    imageBytes,
                    ObjectUtils.asMap("folder", folder, "resource_type", "image"));
            Object secureUrl = result.get("secure_url");
            Object publicId = result.get("public_id");
            if (secureUrl == null || publicId == null) {
                throw new ImageUploadException("업로드 응답에 secure_url/public_id가 없습니다: " + result, null);
            }
            return new UploadedImage(secureUrl.toString(), publicId.toString());
        } catch (IOException e) {
            throw new ImageUploadException("이미지 업로드에 실패했습니다.", e);
        }
    }

    @Override
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (Exception e) {
            // 호출부는 보통 이미 다른 에러(예: 중복 업로드로 인한 409)를 응답하는 중이라,
            // 여기서 예외를 던지면 그 응답을 가리게 된다. 실패해도 조용히 넘기고 로그만 남긴다
            // — 최악의 경우 Cloudinary에 안 쓰는 이미지 하나가 남는 정도라 감수할 만하다.
            log.warn("Cloudinary 이미지 삭제 실패 (고아 이미지로 남을 수 있음): publicId={}", publicId, e);
        }
    }
}
