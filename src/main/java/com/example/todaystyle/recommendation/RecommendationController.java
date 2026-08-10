package com.example.todaystyle.recommendation;

import com.example.todaystyle.recommendation.dto.CombinationResponse;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    /** limit 상한 (과도한 조합 폭증 방지). */
    private static final int MAX_LIMIT = 50;

    private final CombinationRecommendationService recommendationService;

    public RecommendationController(CombinationRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /** 아직 입어본 적 없는 상의×하의 조합 추천 (점수 높은 순). */
    @GetMapping("/combos")
    public List<CombinationResponse> combos(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        int safeLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
        return recommendationService.recommendCombos(userId, safeLimit);
    }
}
