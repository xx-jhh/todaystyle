package com.example.todaystyle.ootd.dto;

import jakarta.validation.constraints.Size;

/** 코디 메모 수정 요청. 빈 문자열/null이면 메모를 지운다. */
public record UpdateMemoRequest(
        @Size(max = 2000) String memo
) {
}
