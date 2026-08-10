package com.example.todaystyle.ootd;

import com.example.todaystyle.ootd.dto.OotdResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    /** 오늘의 착장 사진 업로드. multipart/form-data (image 파일 + recordDate). */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OotdResponse> upload(
            @AuthenticationPrincipal Long userId,
            @RequestParam("recordDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate recordDate,
            @RequestParam("image") MultipartFile image
    ) {
        OotdResponse response = ootdService.upload(userId, recordDate, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** 내 OOTD 목록 (최신 착장 날짜순). */
    @GetMapping
    public List<OotdResponse> list(@AuthenticationPrincipal Long userId) {
        return ootdService.list(userId);
    }

    /** 내 OOTD 단건 조회. */
    @GetMapping("/{id}")
    public OotdResponse get(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return ootdService.get(userId, id);
    }
}
