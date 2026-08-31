package com.example.todaystyle.ootd;

import com.example.todaystyle.common.PageResponse;
import com.example.todaystyle.ootd.dto.OotdCountResponse;
import com.example.todaystyle.ootd.dto.OotdResponse;
import com.example.todaystyle.ootd.dto.UpdateMemoRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ootd")
public class OotdController {

    private final OotdService ootdService;

    public OotdController(OotdService ootdService) {
        this.ootdService = ootdService;
    }

    /**
     * 오늘의 착장 사진 업로드. multipart/form-data (image 파일 + recordDate).
     * lat/lon을 함께 보내면 업로드 시점 날씨를 스냅샷으로 저장한다(선택, 실패해도 업로드는 성공).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OotdResponse> upload(
            @AuthenticationPrincipal Long userId,
            @RequestParam("recordDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate recordDate,
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lon", required = false) Double lon
    ) {
        OotdResponse response = ootdService.upload(userId, recordDate, image, lat, lon);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** 내 OOTD 목록 (최신 착장 날짜순, 페이지네이션). */
    @GetMapping
    public PageResponse<OotdResponse> list(
            @AuthenticationPrincipal Long userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ootdService.list(userId, pageable);
    }

    /** 내 OOTD 전체 개수(마이페이지 통계용). 목록 API가 페이지네이션이라 별도로 내려준다. */
    @GetMapping("/count")
    public OotdCountResponse count(@AuthenticationPrincipal Long userId) {
        return ootdService.count(userId);
    }

    /** 내 OOTD 단건 조회. */
    @GetMapping("/{id}")
    public OotdResponse get(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return ootdService.get(userId, id);
    }

    /** 코디 메모 작성/수정. 빈 문자열을 보내면 메모를 지운다. */
    @PatchMapping("/{id}")
    public OotdResponse updateMemo(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateMemoRequest request
    ) {
        return ootdService.updateMemo(userId, id, request.memo());
    }

    /** OOTD 삭제. 잘못 찍었거나 마음에 안 드는 사진을 지우고 다시 올릴 수 있게 한다. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        ootdService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
