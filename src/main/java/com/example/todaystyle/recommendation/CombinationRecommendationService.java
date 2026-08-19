package com.example.todaystyle.recommendation;

import com.example.todaystyle.clothing.ClothingCategory;
import com.example.todaystyle.clothing.ClothingItem;
import com.example.todaystyle.clothing.ClothingItemRepository;
import com.example.todaystyle.clothing.Fit;
import com.example.todaystyle.clothing.dto.ClothingItemResponse;
import com.example.todaystyle.recommendation.dto.CombinationResponse;
import com.example.todaystyle.user.BodyType;
import com.example.todaystyle.user.StyleCategory;
import com.example.todaystyle.user.User;
import com.example.todaystyle.user.UserRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 코디 조합 추천의 핵심 로직. 서로 다른 OOTD(=다른 날짜)에서 나온 상의와 하의 중
 * 실제로 함께 입은 적 없는 페어를 색상 조합·체형별 핏·선호 스타일별 핏으로 점수화해 추천한다.
 * 초기에는 규칙 기반이며, 데이터가 쌓이면 임베딩 유사도로 고도화한다.
 */
@Service
public class CombinationRecommendationService {

    /** 총점 가중치. 색상 조합을 가장 크게 보고, 체형 핏을 스타일 핏보다 조금 더 중요하게 본다. */
    private static final double COLOR_WEIGHT = 0.5;
    private static final double BODY_FIT_WEIGHT = 0.3;
    private static final double STYLE_FIT_WEIGHT = 0.2;

    private final ClothingItemRepository clothingItemRepository;
    private final UserRepository userRepository;

    public CombinationRecommendationService(
            ClothingItemRepository clothingItemRepository,
            UserRepository userRepository
    ) {
        this.clothingItemRepository = clothingItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<CombinationResponse> recommendCombos(Long userId, int limit) {
        User user = userRepository.findById(userId).orElse(null);
        BodyType bodyType = user == null ? null : user.getBodyType();
        StyleCategory preferredStyle = user == null ? null : user.getPreferredStyle();

        List<ClothingItem> tops =
                clothingItemRepository.findByUserIdAndCategoryWithOotd(userId, ClothingCategory.TOP);
        List<ClothingItem> bottoms =
                clothingItemRepository.findByUserIdAndCategoryWithOotd(userId, ClothingCategory.BOTTOM);

        List<CombinationResponse> results = new ArrayList<>();
        for (ClothingItem top : tops) {
            for (ClothingItem bottom : bottoms) {
                // 같은 OOTD(같은 날) 소속이면 이미 함께 입은 조합이므로 제외한다.
                if (top.getOotdRecord().getId().equals(bottom.getOotdRecord().getId())) {
                    continue;
                }
                results.add(score(top, bottom, bodyType, preferredStyle));
            }
        }

        results.sort(Comparator.comparingDouble(CombinationResponse::score).reversed());
        return results.size() > limit ? results.subList(0, limit) : results;
    }

    private CombinationResponse score(
            ClothingItem top, ClothingItem bottom, BodyType bodyType, StyleCategory preferredStyle
    ) {
        ColorHarmony.Result color = ColorHarmony.evaluate(top.getColor(), bottom.getColor());
        double bodyFitScore = fitScore(
                top.getFit(), bottom.getFit(), bodyType != null, fit -> BodyTypeStyleRules.matches(bodyType, fit));
        double styleFitScore = fitScore(
                top.getFit(), bottom.getFit(), preferredStyle != null, fit -> StyleFitRules.matches(preferredStyle, fit));

        double total = COLOR_WEIGHT * color.score()
                + BODY_FIT_WEIGHT * bodyFitScore
                + STYLE_FIT_WEIGHT * styleFitScore;
        String reason = buildReason(color.label(), bodyType, preferredStyle, top.getFit(), bottom.getFit());

        return new CombinationResponse(
                ClothingItemResponse.from(top),
                ClothingItemResponse.from(bottom),
                round2(total),
                reason);
    }

    /**
     * bodyType/preferredStyle 자체가 없으면(hasPreference=false) 규칙표를 아예 조회하지 않고
     * 중립값을 준다 — null 체형/스타일로 매칭 규칙을 호출하면 예외가 나기도 하고, 애초에
     * "선호를 모른다"와 "선호에 안 맞는다"는 의미가 다르므로 구분해야 한다.
     */
    private double fitScore(Fit topFit, Fit bottomFit, boolean hasPreference, Predicate<Fit> matcher) {
        if (!hasPreference) {
            return 0.5;
        }
        return (fitItemScore(topFit, matcher) + fitItemScore(bottomFit, matcher)) / 2.0;
    }

    private double fitItemScore(Fit fit, Predicate<Fit> matcher) {
        if (fit == null) {
            return 0.5;
        }
        return matcher.test(fit) ? 1.0 : 0.4;
    }

    private String buildReason(
            String colorLabel, BodyType bodyType, StyleCategory preferredStyle, Fit topFit, Fit bottomFit
    ) {
        List<String> parts = new ArrayList<>();
        parts.add(colorLabel);
        addFitNote(parts, "체형", bodyType != null, fit -> BodyTypeStyleRules.matches(bodyType, fit), topFit, bottomFit);
        addFitNote(parts, "스타일", preferredStyle != null,
                fit -> StyleFitRules.matches(preferredStyle, fit), topFit, bottomFit);
        return String.join(" · ", parts);
    }

    private void addFitNote(
            List<String> parts, String label, boolean hasPreference, Predicate<Fit> matcher, Fit topFit, Fit bottomFit
    ) {
        if (!hasPreference || (topFit == null && bottomFit == null)) {
            return;
        }
        boolean topOk = topFit != null && matcher.test(topFit);
        boolean bottomOk = bottomFit != null && matcher.test(bottomFit);
        String note = (topOk && bottomOk) ? label + "에 맞는 핏"
                : (topOk || bottomOk) ? label + " 핏 무난"
                : label + "과는 다소 어긋난 핏";
        parts.add(note);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
