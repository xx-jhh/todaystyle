package com.example.todaystyle.ootd;

import com.example.todaystyle.common.storage.ImageStorageService;
import com.example.todaystyle.ootd.dto.OotdResponse;
import com.example.todaystyle.user.UserRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OotdService {

    private final OotdRepository ootdRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;

    public OotdService(
            OotdRepository ootdRepository,
            UserRepository userRepository,
            ImageStorageService imageStorageService
    ) {
        this.ootdRepository = ootdRepository;
        this.userRepository = userRepository;
        this.imageStorageService = imageStorageService;
    }

    @Transactional
    public OotdResponse upload(Long userId, LocalDate recordDate, MultipartFile image) {
        if (ootdRepository.existsByUserIdAndRecordDate(userId, recordDate)) {
            throw new OotdAlreadyExistsException(recordDate);
        }

        String photoUrl = imageStorageService.upload(image, "todaystyle/ootd/" + userId);

        OotdRecord record = new OotdRecord();
        record.setUser(userRepository.getReferenceById(userId));
        record.setRecordDate(recordDate);
        record.setPhotoUrl(photoUrl);

        // TODO: 이미지 인식 API 연동 시, 여기서 업로드한 사진을 분석해
        //       ClothingItem(category/color/fit)을 자동 추출·저장한다.
        return OotdResponse.from(ootdRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<OotdResponse> list(Long userId) {
        return ootdRepository.findByUserIdOrderByRecordDateDesc(userId).stream()
                .map(OotdResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OotdResponse get(Long userId, Long id) {
        OotdRecord record = ootdRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new OotdNotFoundException(id));
        return OotdResponse.from(record);
    }
}
