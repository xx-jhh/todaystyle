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
import java.util.Set;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 코디 조합 추천의 핵심 로직. 서로 다른 OOTD(=다른 날짜)에서 나온 두 아이템 중 실제로 함께
 * 입은 적 없는 페어를 색상 조합·체형별 핏·선호 스타일별 핏으로 점수화해 추천한다. 상의×하의뿐
 * 아니라 원피스×아우터도 같은 방식으로 채점한다 — 원피스 위주로 옷장을 채우는 사용자는
 * 상의/하의가 아예 없어서 예전엔 추천이 항상 빈 목록이었다. 초기에는 규칙 기반이며,
 * 데이터가 쌓이면 임베딩 유사도로 고도화한다.
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
        Set<StyleCategory> preferredStyles = user == null ? Set.of() : user.getPreferredStyles();

        List<ClothingItem> tops = itemsByCategory(userId, ClothingCategory.TOP);
        List<ClothingItem> bottoms = itemsByCategory(userId, ClothingCategory.BOTTOM);
        List<ClothingItem> dresses = itemsByCategory(userId, ClothingCategory.DRESS);
        List<ClothingItem> outers = itemsByCategory(userId, ClothingCategory.OUTER);

        List<CombinationResponse> results = new ArrayList<>();
        addCombos(results, tops, bottoms, bodyType, preferredStyles);
        addCombos(results, dresses, outers, bodyType, preferredStyles);

        results.sort(Comparator.comparingDouble(CombinationResponse::score).reversed());
        return results.size() > limit ? results.subList(0, limit) : results;
    }

    private List<ClothingItem> itemsByCategory(Long userId, ClothingCategory category) {
        return clothingItemRepository.findByUserIdAndCategoryWithOotd(userId, category);
    }

    private void addCombos(
            List<CombinationResponse> results, List<ClothingItem> primaryItems, List<ClothingItem> secondaryItems,
            BodyType bodyType, Set<StyleCategory> preferredStyles
    ) {
        for (ClothingItem primary : primaryItems) {
            for (ClothingItem secondary : secondaryItems) {
                // 같은 OOTD(같은 날) 소속이면 이미 함께 입은 조합이므로 제외한다.
                if (primary.getOotdRecord().getId().equals(secondary.getOotdRecord().getId())) {
                    continue;
                }
                results.add(score(primary, secondary, bodyType, preferredStyles));
            }
        }
    }

    private CombinationResponse score(
            ClothingItem primary, ClothingItem secondary, BodyType bodyType, Set<StyleCategory> preferredStyles
    ) {
        ColorHarmony.Result color = ColorHarmony.evaluate(primary.getColor(), secondary.getColor());
        double bodyFitScore = fitScore(
                primary.getFit(), secondary.getFit(), bodyType != null,
                fit -> BodyTypeStyleRules.matches(bodyType, fit));
        double styleFitScore = fitScore(
                primary.getFit(), secondary.getFit(), !preferredStyles.isEmpty(),
                fit -> matchesAnyStyle(preferredStyles, fit));

        double total = COLOR_WEIGHT * color.score()
                + BODY_FIT_WEIGHT * bodyFitScore
                + STYLE_FIT_WEIGHT * styleFitScore;
        String reason = buildReason(color.label(), bodyType, preferredStyles, primary.getFit(), secondary.getFit());

        return new CombinationResponse(
                ClothingItemResponse.from(primary),
                ClothingItemResponse.from(secondary),
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
            String colorLabel, BodyType bodyType, Set<StyleCategory> preferredStyles, Fit topFit, Fit bottomFit
    ) {
        List<String> parts = new ArrayList<>();
        parts.add(colorLabel);
        addFitNote(parts, "체형", bodyType != null, fit -> BodyTypeStyleRules.matches(bodyType, fit), topFit, bottomFit);
        addFitNote(parts, "스타일", !preferredStyles.isEmpty(),
                fit -> matchesAnyStyle(preferredStyles, fit), topFit, bottomFit);
        return String.join(" · ", parts);
    }

    /** 선호 스타일을 여러 개 골랐으면, 그중 하나라도 맞는 핏이면 "스타일에 맞는다"고 인정한다. */
    private boolean matchesAnyStyle(Set<StyleCategory> preferredStyles, Fit fit) {
        return preferredStyles.stream().anyMatch(style -> StyleFitRules.matches(style, fit));
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
