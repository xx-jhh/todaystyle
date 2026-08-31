package com.example.todaystyle.ootd.dto;

/** 마이페이지 통계용. 목록 API가 페이지네이션으로 바뀌면서 전체 개수를 따로 내려줘야 한다. */
public record OotdCountResponse(long count) {
}
