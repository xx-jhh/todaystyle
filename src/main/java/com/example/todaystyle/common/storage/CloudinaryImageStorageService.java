package com.example.todaystyle.common.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryImageStorageService implements ImageStorageService {

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    public CloudinaryImageStorageService(Cloudinary cloudinary, CloudinaryProperties properties) {
        this.cloudinary = cloudinary;
        this.properties = properties;
    }

    @Override
    public String upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 비어 있습니다.");
        }
        if (!properties.isConfigured()) {
            throw new ImageUploadException("Cloudinary 자격증명이 설정되지 않았습니다. "
                    + "CLOUDINARY_CLOUD_NAME / CLOUDINARY_API_KEY / CLOUDINARY_API_SECRET 를 설정하세요.", null);
        }
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", folder, "resource_type", "image"));
            Object secureUrl = result.get("secure_url");
            if (secureUrl == null) {
                throw new ImageUploadException("업로드 응답에 secure_url이 없습니다: " + result, null);
            }
            return secureUrl.toString();
        } catch (IOException e) {
            throw new ImageUploadException("이미지 업로드에 실패했습니다.", e);
        }
    }
}
