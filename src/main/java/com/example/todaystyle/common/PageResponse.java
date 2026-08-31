package com.example.todaystyle.common;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/**
 * 목록 API의 페이지네이션 응답 포맷. 프론트가 "더 보기" 버튼 노출 여부만 알면 되므로
 * 총 개수/전체 페이지 수 대신 hasNext만 내려준다.
 */
public record PageResponse<T>(List<T> items, boolean hasNext) {

    public static <S, T> PageResponse<T> of(Page<S> page, Function<S, T> mapper) {
        return new PageResponse<>(page.getContent().stream().map(mapper).toList(), page.hasNext());
    }
}
