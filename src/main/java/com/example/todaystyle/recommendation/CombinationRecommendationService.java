package com.example.todaystyle.recommendation;

import com.example.todaystyle.clothing.ClothingCategory;
import com.example.todaystyle.clothing.ClothingItem;
import com.example.todaystyle.clothing.ClothingItemRepository;
import com.example.todaystyle.clothing.Fit;
import com.example.todaystyle.clothing.dto.ClothingItemResponse;
import com.example.todaystyle.recommendation.dto.CombinationResponse;
import com.example.todaystyle.user.BodyType;
import com.example.todaystyle.user.User;
import com.example.todaystyle.user.UserRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 코디 조합 추천의 핵심 로직. 서로 다른 OOTD(=다른 날짜)에서 나온 상의와 하의 중
 * 실제로 함께 입은 적 없는 페어를 색상 조합과 체형별 핏 규칙으로 점수화해 추천한다.
 * 초기에는 규칙 기반이며, 데이터가 쌓이면 임베딩 유사도로 고도화한다.
 */
@Service
public class CombinationRecommendationService {

    /** 총점에서 색상 조합이 차지하는 가중치. 나머지는 체형 핏 점수. */
    private static final double COLOR_WEIGHT = 0.6;
    private static final double FIT_WEIGHT = 0.4;

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
        BodyType bodyType = userRepository.findById(userId)
                .map(User::getBodyType)
                .orElse(null);

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
                results.add(score(top, bottom, bodyType));
            }
        }

        results.sort(Comparator.comparingDouble(CombinationResponse::score).reversed());
        return results.size() > limit ? results.subList(0, limit) : results;
    }

    private CombinationResponse score(ClothingItem top, ClothingItem bottom, BodyType bodyType) {
        ColorHarmony.Result color = ColorHarmony.evaluate(top.getColor(), bottom.getColor());
        double fitScore = fitScore(bodyType, top.getFit(), bottom.getFit());

        double total = COLOR_WEIGHT * color.score() + FIT_WEIGHT * fitScore;
        String reason = buildReason(color.label(), bodyType, top.getFit(), bottom.getFit());

        return new CombinationResponse(
                ClothingItemResponse.from(top),
                ClothingItemResponse.from(bottom),
                round2(total),
                reason);
    }

    private double fitScore(BodyType bodyType, Fit topFit, Fit bottomFit) {
        if (bodyType == null) {
            return 0.5;
        }
        return (fitItemScore(bodyType, topFit) + fitItemScore(bodyType, bottomFit)) / 2.0;
    }

    private double fitItemScore(BodyType bodyType, Fit fit) {
        if (fit == null) {
            return 0.5;
        }
        return BodyTypeStyleRules.matches(bodyType, fit) ? 1.0 : 0.4;
    }

    private String buildReason(String colorLabel, BodyType bodyType, Fit topFit, Fit bottomFit) {
        if (bodyType == null || (topFit == null && bottomFit == null)) {
            return colorLabel;
        }
        boolean topOk = topFit != null && BodyTypeStyleRules.matches(bodyType, topFit);
        boolean bottomOk = bottomFit != null && BodyTypeStyleRules.matches(bodyType, bottomFit);
        String fitNote = (topOk && bottomOk) ? "체형에 맞는 핏"
                : (topOk || bottomOk) ? "핏 무난"
                : "체형과는 다소 어긋난 핏";
        return colorLabel + " · " + fitNote;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
