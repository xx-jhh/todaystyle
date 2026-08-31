package com.example.todaystyle.ootd;

import java.nio.charset.StandardCharsets;

/**
 * 파일 시그니처(매직 바이트)로 실제 이미지 형식을 판별한다. {@code MultipartFile.getContentType()}은
 * 클라이언트가 보내는 값이라 위조 가능해서(예: .exe에 Content-Type: image/png를 붙여 보내는 경우),
 * Cloudinary/이미지 인식 파이프라인에 넘기기 전에 실제 바이트를 한 번 더 확인한다.
 * 스마트폰 카메라 롤에서 흔한 JPEG/PNG/HEIC(아이폰 기본)/WEBP와, GIF/BMP까지 폭넓게 인정한다.
 */
final class ImageSignature {

    private ImageSignature() {
    }

    static boolean looksLikeImage(byte[] bytes) {
        return isJpeg(bytes) || isPng(bytes) || isGif(bytes) || isBmp(bytes) || isWebp(bytes) || isHeif(bytes);
    }

    private static boolean isJpeg(byte[] b) {
        return startsWith(b, 0xFF, 0xD8, 0xFF);
    }

    private static boolean isPng(byte[] b) {
        return startsWith(b, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
    }

    private static boolean isGif(byte[] b) {
        return startsWith(b, 'G', 'I', 'F', '8') && b.length > 4 && (b[4] == '7' || b[4] == '9');
    }

    private static boolean isBmp(byte[] b) {
        return startsWith(b, 'B', 'M');
    }

    private static boolean isWebp(byte[] b) {
        return b.length >= 12
                && startsWith(b, 'R', 'I', 'F', 'F')
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
    }

    /** HEIC/HEIF(아이폰 기본 사진 포맷) — ISO BMFF 컨테이너의 ftyp 박스와 브랜드로 판별. */
    private static boolean isHeif(byte[] b) {
        if (b.length < 12 || b[4] != 'f' || b[5] != 't' || b[6] != 'y' || b[7] != 'p') {
            return false;
        }
        String brand = new String(b, 8, 4, StandardCharsets.US_ASCII);
        return switch (brand) {
            case "heic", "heix", "hevc", "hevx", "mif1", "msf1", "heim", "heis" -> true;
            default -> false;
        };
    }

    private static boolean startsWith(byte[] data, int... expectedUnsignedBytes) {
        if (data.length < expectedUnsignedBytes.length) {
            return false;
        }
        for (int i = 0; i < expectedUnsignedBytes.length; i++) {
            if ((data[i] & 0xFF) != expectedUnsignedBytes[i]) {
                return false;
            }
        }
        return true;
    }

    private static boolean startsWith(byte[] data, char... expectedAscii) {
        if (data.length < expectedAscii.length) {
            return false;
        }
        for (int i = 0; i < expectedAscii.length; i++) {
            if (data[i] != (byte) expectedAscii[i]) {
                return false;
            }
        }
        return true;
    }
}
