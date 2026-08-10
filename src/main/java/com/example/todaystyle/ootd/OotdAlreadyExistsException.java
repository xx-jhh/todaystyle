package com.example.todaystyle.ootd;

import java.time.LocalDate;

/** 같은 날짜에 이미 OOTD를 업로드한 경우 (하루 1건 제약). */
public class OotdAlreadyExistsException extends RuntimeException {

    public OotdAlreadyExistsException(LocalDate recordDate) {
        super("해당 날짜에 이미 업로드한 OOTD가 있습니다: " + recordDate);
    }
}
