package com.example.todaystyle.recommendation;

import com.example.todaystyle.clothing.Fit;
import com.example.todaystyle.user.BodyType;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 체형별로 어울리는 핏을 정의하는 규칙표. 색상 조합 추천과 마찬가지로 초기에는
 * 코드 기반 규칙으로 시작하고, 데이터가 쌓이면 고도화한다. 값은 일반적인 스타일링
 * 가이드를 참고한 초안이므로 검토 후 조정한다.
 */
public final class BodyTypeStyleRules {

    private static final Map<BodyType, Set<Fit>> RECOMMENDED_FITS = Map.of(
            BodyType.SLIM, EnumSet.of(Fit.LOOSE, Fit.OVERSIZED),
            BodyType.STANDARD, EnumSet.of(Fit.REGULAR, Fit.SLIM),
            BodyType.ATHLETIC, EnumSet.of(Fit.REGULAR, Fit.LOOSE),
            BodyType.CHUBBY, EnumSet.of(Fit.REGULAR, Fit.LOOSE),
            BodyType.TALL_SLIM, EnumSet.of(Fit.SLIM, Fit.REGULAR)
    );

    private BodyTypeStyleRules() {
    }

    public static Set<Fit> recommendedFitsFor(BodyType bodyType) {
        return RECOMMENDED_FITS.getOrDefault(bodyType, EnumSet.allOf(Fit.class));
    }

    public static boolean matches(BodyType bodyType, Fit fit) {
        return fit != null && recommendedFitsFor(bodyType).contains(fit);
    }
}
