package com.example.todaystyle.ootd.dto;

import com.example.todaystyle.ootd.OotdRecord;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record OotdResponse(
        Long id,
        LocalDate recordDate,
        String photoUrl,
        LocalDateTime createdAt
) {
    public static OotdResponse from(OotdRecord record) {
        return new OotdResponse(
                record.getId(),
                record.getRecordDate(),
                record.getPhotoUrl(),
                record.getCreatedAt()
        );
    }
}
