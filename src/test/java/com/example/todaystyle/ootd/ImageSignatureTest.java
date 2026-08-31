package com.example.todaystyle.ootd;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ImageSignatureTest {

    @Test
    void JPEG_시그니처를_인식한다() {
        byte[] bytes = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0};
        assertThat(ImageSignature.looksLikeImage(bytes)).isTrue();
    }

    @Test
    void PNG_시그니처를_인식한다() {
        byte[] bytes = {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0
        };
        assertThat(ImageSignature.looksLikeImage(bytes)).isTrue();
    }

    @Test
    void WEBP_시그니처를_인식한다() {
        byte[] bytes = "RIFF????WEBP".getBytes();
        assertThat(ImageSignature.looksLikeImage(bytes)).isTrue();
    }

    @Test
    void HEIC_시그니처를_인식한다() {
        // ISO BMFF: [4바이트 박스크기][ftyp][heic]
        byte[] bytes = {0, 0, 0, 0x18, 'f', 't', 'y', 'p', 'h', 'e', 'i', 'c', 0, 0, 0, 0};
        assertThat(ImageSignature.looksLikeImage(bytes)).isTrue();
    }

    @Test
    void GIF_시그니처를_인식한다() {
        assertThat(ImageSignature.looksLikeImage("GIF89a".getBytes())).isTrue();
    }

    @Test
    void BMP_시그니처를_인식한다() {
        assertThat(ImageSignature.looksLikeImage("BM????".getBytes())).isTrue();
    }

    @Test
    void 이미지가_아닌_바이트는_거부한다() {
        assertThat(ImageSignature.looksLikeImage("this is just plain text".getBytes())).isFalse();
        assertThat(ImageSignature.looksLikeImage("<html>fake</html>".getBytes())).isFalse();
    }

    @Test
    void 빈_바이트배열은_거부한다() {
        assertThat(ImageSignature.looksLikeImage(new byte[0])).isFalse();
    }
}
