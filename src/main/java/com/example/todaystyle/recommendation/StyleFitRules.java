package com.example.todaystyle.recommendation;

import com.example.todaystyle.clothing.Fit;
import com.example.todaystyle.user.StyleCategory;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 회원가입 시 고른 선호 스타일별로 어울리는 핏을 정의하는 규칙표.
 * {@link BodyTypeStyleRules}와 같은 방식으로 코드 기반 규칙에서 시작하고, 데이터가 쌓이면
 * 고도화한다. 값은 일반적인 스타일링 가이드를 참고한 초안이므로 검토 후 조정한다.
 */
public final class StyleFitRules {

    private static final Map<StyleCategory, Set<Fit>> RECOMMENDED_FITS = Map.of(
            // 캐주얼은 폭넓게 무난히 받아들이는 스타일이라 정돈된 핏 위주로.
            StyleCategory.CASUAL, EnumSet.of(Fit.REGULAR, Fit.LOOSE),
            // 아메카지(빈티지 워크웨어 기반)는 살짝 여유 있는 핏이 기본.
            StyleCategory.AMEKAJI, EnumSet.of(Fit.REGULAR, Fit.LOOSE),
            // 스트릿은 루즈/오버사이즈가 스타일의 핵심.
            StyleCategory.STREET, EnumSet.of(Fit.LOOSE, Fit.OVERSIZED),
            // 미니멀은 군더더기 없는 정돈된 실루엣이 핵심이라 슬림/레귤러.
            StyleCategory.MINIMAL, EnumSet.of(Fit.SLIM, Fit.REGULAR),
            // 포멀은 테일러드된 핏이 기본.
            StyleCategory.FORMAL, EnumSet.of(Fit.SLIM, Fit.REGULAR),
            // 빈티지는 옛날 옷 특유의 여유로운 핏이 자연스럽다.
            StyleCategory.VINTAGE, EnumSet.of(Fit.REGULAR, Fit.LOOSE)
    );

    private StyleFitRules() {
    }

    public static Set<Fit> recommendedFitsFor(StyleCategory style) {
        return RECOMMENDED_FITS.getOrDefault(style, EnumSet.allOf(Fit.class));
    }

    public static boolean matches(StyleCategory style, Fit fit) {
        return fit != null && recommendedFitsFor(style).contains(fit);
    }
}
