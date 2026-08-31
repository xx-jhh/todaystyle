package com.example.todaystyle.ootd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.todaystyle.clothing.ClothingItemRepository;
import com.example.todaystyle.common.storage.ImageStorageService;
import com.example.todaystyle.ootd.dto.OotdResponse;
import com.example.todaystyle.user.UserRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class OotdServiceTest {

    private static final Long USER_ID = 1L;
    private static final LocalDate RECORD_DATE = LocalDate.of(2026, 8, 19);
    /** 실제 JPEG 파일 시그니처(FF D8 FF)로 시작하는 더미 바이트 — ImageSignature 검증을 통과시키기 위함. */
    private static final byte[] JPEG_MAGIC_BYTES = {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 'f', 'a', 'k', 'e'
    };

    @Mock
    private OotdRepository ootdRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ImageStorageService imageStorageService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private ClothingItemRepository clothingItemRepository;

    private OotdService ootdService;

    @BeforeEach
    void setUp() {
        ootdService = new OotdService(
                ootdRepository, userRepository, imageStorageService, eventPublisher, clothingItemRepository);
    }

    @Test
    void 빈_파일이면_업로드_전에_거부하고_외부_호출을_하지_않는다() {
        MultipartFile emptyFile = new MockMultipartFile("image", "photo.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> ootdService.upload(USER_ID, RECORD_DATE, emptyFile, null, null))
                .isInstanceOf(InvalidImageException.class);

        verifyNoInteractions(imageStorageService, eventPublisher);
        verify(ootdRepository, never()).save(any());
    }

    @Test
    void 이미지가_아닌_파일이면_업로드_전에_거부하고_외부_호출을_하지_않는다() {
        MultipartFile pdfFile = new MockMultipartFile(
                "image", "document.pdf", "application/pdf", "not an image".getBytes());

        assertThatThrownBy(() -> ootdService.upload(USER_ID, RECORD_DATE, pdfFile, null, null))
                .isInstanceOf(InvalidImageException.class);

        verifyNoInteractions(imageStorageService, eventPublisher);
        verify(ootdRepository, never()).save(any());
    }

    @Test
    void contentType이_없는_파일이면_거부한다() {
        MultipartFile noContentType = new MockMultipartFile("image", "photo", null, "bytes".getBytes());

        assertThatThrownBy(() -> ootdService.upload(USER_ID, RECORD_DATE, noContentType, null, null))
                .isInstanceOf(InvalidImageException.class);
    }

    @Test
    void contentType은_image로_속였지만_실제_바이트가_이미지가_아니면_거부한다() {
        when(ootdRepository.existsByUserIdAndRecordDate(USER_ID, RECORD_DATE)).thenReturn(false);
        MultipartFile spoofed = new MockMultipartFile(
                "image", "malware.png", "image/png", "not actually an image".getBytes());

        assertThatThrownBy(() -> ootdService.upload(USER_ID, RECORD_DATE, spoofed, null, null))
                .isInstanceOf(InvalidImageException.class);

        verifyNoInteractions(imageStorageService, eventPublisher);
        verify(ootdRepository, never()).save(any());
    }

    @Test
    void 정상_이미지는_업로드되고_보강_이벤트가_발행된다() {
        MultipartFile image = new MockMultipartFile("image", "photo.jpg", "image/jpeg", JPEG_MAGIC_BYTES);
        when(ootdRepository.existsByUserIdAndRecordDate(USER_ID, RECORD_DATE)).thenReturn(false);
        when(imageStorageService.upload(any(byte[].class), eq("todaystyle/ootd/" + USER_ID)))
                .thenReturn("https://cdn.example.com/photo.jpg");
        when(ootdRepository.save(any(OotdRecord.class))).thenAnswer(invocation -> {
            OotdRecord saved = invocation.getArgument(0);
            saved.setId(42L);
            return saved;
        });

        OotdResponse response = ootdService.upload(USER_ID, RECORD_DATE, image, 37.5665, 126.978);

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.photoUrl()).isEqualTo("https://cdn.example.com/photo.jpg");

        ArgumentCaptor<OotdUploadedEvent> eventCaptor = ArgumentCaptor.forClass(OotdUploadedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        OotdUploadedEvent event = eventCaptor.getValue();
        assertThat(event.ootdRecordId()).isEqualTo(42L);
        assertThat(event.userId()).isEqualTo(USER_ID);
        assertThat(event.lat()).isEqualTo(37.5665);
        assertThat(event.lon()).isEqualTo(126.978);
        assertThat(event.imageContentType()).isEqualTo("image/jpeg");
    }

    @Test
    void 같은_날짜에_이미_업로드했으면_예외를_던진다() {
        MultipartFile image = new MockMultipartFile("image", "photo.jpg", "image/jpeg", "fake-image-bytes".getBytes());
        when(ootdRepository.existsByUserIdAndRecordDate(USER_ID, RECORD_DATE)).thenReturn(true);

        assertThatThrownBy(() -> ootdService.upload(USER_ID, RECORD_DATE, image, null, null))
                .isInstanceOf(OotdAlreadyExistsException.class);

        verifyNoInteractions(imageStorageService, eventPublisher);
    }

    @Test
    void 삭제하면_옷_아이템을_먼저_지운_뒤_OOTD_레코드를_지운다() {
        OotdRecord record = new OotdRecord();
        record.setId(42L);
        when(ootdRepository.findByIdAndUserId(42L, USER_ID)).thenReturn(Optional.of(record));

        ootdService.delete(USER_ID, 42L);

        InOrder order = inOrder(clothingItemRepository, ootdRepository);
        order.verify(clothingItemRepository).deleteByOotdRecordId(42L);
        order.verify(ootdRepository).delete(record);
    }

    @Test
    void 존재하지_않거나_남의_OOTD를_삭제하려_하면_예외를_던진다() {
        when(ootdRepository.findByIdAndUserId(99L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ootdService.delete(USER_ID, 99L))
                .isInstanceOf(OotdNotFoundException.class);

        verifyNoInteractions(clothingItemRepository);
    }
}
