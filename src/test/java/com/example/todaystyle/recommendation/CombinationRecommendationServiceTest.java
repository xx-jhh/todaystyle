package com.example.todaystyle.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.todaystyle.clothing.ClothingCategory;
import com.example.todaystyle.clothing.ClothingItem;
import com.example.todaystyle.clothing.ClothingItemRepository;
import com.example.todaystyle.clothing.Fit;
import com.example.todaystyle.ootd.OotdRecord;
import com.example.todaystyle.recommendation.dto.CombinationResponse;
import com.example.todaystyle.user.BodyType;
import com.example.todaystyle.user.StyleCategory;
import com.example.todaystyle.user.User;
import com.example.todaystyle.user.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CombinationRecommendationServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private ClothingItemRepository clothingItemRepository;
    @Mock
    private UserRepository userRepository;

    private CombinationRecommendationService service;

    @BeforeEach
    void setUp() {
        service = new CombinationRecommendationService(clothingItemRepository, userRepository);
    }

    @Test
    void 같은_OOTD에_속한_상하의_조합은_추천에서_제외한다() {
        User user = user(BodyType.STRAIGHT, StyleCategory.CASUAL);
        OotdRecord sameDay = ootd(101L, LocalDate.of(2026, 8, 1));
        ClothingItem top = item(user, sameDay, ClothingCategory.TOP, "#FF0000", Fit.REGULAR);
        ClothingItem bottom = item(user, sameDay, ClothingCategory.BOTTOM, "#00FFFF", Fit.REGULAR);
        givenUserAndItems(user, List.of(top), List.of(bottom));

        List<CombinationResponse> results = service.recommendCombos(USER_ID, 10);

        assertThat(results).isEmpty();
    }

    @Test
    void 체형과_스타일에_모두_맞는_조합이_둘_다_어긋나는_조합보다_높은_점수를_받는다() {
        User user = user(BodyType.STRAIGHT, StyleCategory.CASUAL);
        OotdRecord topDay = ootd(101L, LocalDate.of(2026, 8, 1));
        OotdRecord bottomDayGood = ootd(102L, LocalDate.of(2026, 8, 2));
        OotdRecord bottomDayBad = ootd(103L, LocalDate.of(2026, 8, 3));

        // 색상은 두 경우 모두 모노톤(0.90)으로 동일하게 둬서, 체형·스타일 핏 차이만으로 점수가 갈리는지 본다.
        // REGULAR는 STRAIGHT(체형)·CASUAL(스타일) 둘 다 만족 → bodyFit=1.0, styleFit=1.0.
        // 0.5*0.90 + 0.3*1.0 + 0.2*1.0 = 0.95
        ClothingItem top = item(user, topDay, ClothingCategory.TOP, "#FF0000", Fit.REGULAR);
        ClothingItem goodBottom = item(user, bottomDayGood, ClothingCategory.BOTTOM, "#FF2B00", Fit.REGULAR);
        // OVERSIZED는 STRAIGHT·CASUAL 둘 다 불만족 → bodyFit=(1.0+0.4)/2=0.7, styleFit도 0.7.
        // 0.5*0.90 + 0.3*0.7 + 0.2*0.7 = 0.80
        ClothingItem badBottom = item(user, bottomDayBad, ClothingCategory.BOTTOM, "#FF2B00", Fit.OVERSIZED);

        givenUserAndItems(user, List.of(top), List.of(goodBottom, badBottom));

        List<CombinationResponse> results = service.recommendCombos(USER_ID, 10);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).secondaryItem().id()).isEqualTo(goodBottom.getId());
        assertThat(results.get(0).score()).isEqualTo(0.95);
        assertThat(results.get(1).secondaryItem().id()).isEqualTo(badBottom.getId());
        assertThat(results.get(1).score()).isEqualTo(0.80);
    }

    @Test
    void limit을_넘는_결과는_잘라낸다() {
        User user = user(BodyType.STRAIGHT, StyleCategory.CASUAL);
        OotdRecord topDay = ootd(101L, LocalDate.of(2026, 8, 1));
        ClothingItem top = item(user, topDay, ClothingCategory.TOP, "#FF0000", Fit.REGULAR);
        List<ClothingItem> bottoms = List.of(
                item(user, ootd(201L, LocalDate.of(2026, 8, 2)), ClothingCategory.BOTTOM, "#00FFFF", Fit.REGULAR),
                item(user, ootd(202L, LocalDate.of(2026, 8, 3)), ClothingCategory.BOTTOM, "#00FF00", Fit.REGULAR),
                item(user, ootd(203L, LocalDate.of(2026, 8, 4)), ClothingCategory.BOTTOM, "#0000FF", Fit.REGULAR)
        );
        givenUserAndItems(user, List.of(top), bottoms);

        List<CombinationResponse> results = service.recommendCombos(USER_ID, 2);

        assertThat(results).hasSize(2);
    }

    @Test
    void 체형정보가_없으면_체형_핏_점수만_중립값으로_계산된다() {
        User user = user(null, StyleCategory.CASUAL);
        OotdRecord topDay = ootd(101L, LocalDate.of(2026, 8, 1));
        OotdRecord bottomDay = ootd(102L, LocalDate.of(2026, 8, 2));
        // 색상 모노톤(0.90), 체형 미상(중립 0.5), 스타일은 CASUAL과 REGULAR가 맞음(1.0).
        // 0.5*0.90 + 0.3*0.5 + 0.2*1.0 = 0.80
        ClothingItem top = item(user, topDay, ClothingCategory.TOP, "#FF0000", Fit.REGULAR);
        ClothingItem bottom = item(user, bottomDay, ClothingCategory.BOTTOM, "#FF2B00", Fit.REGULAR);
        givenUserAndItems(user, List.of(top), List.of(bottom));

        List<CombinationResponse> results = service.recommendCombos(USER_ID, 10);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).score()).isEqualTo(0.80);
    }

    @Test
    void 선호_스타일이_없으면_스타일_핏_점수만_중립값으로_계산된다() {
        User user = user(BodyType.STRAIGHT, null);
        OotdRecord topDay = ootd(101L, LocalDate.of(2026, 8, 1));
        OotdRecord bottomDay = ootd(102L, LocalDate.of(2026, 8, 2));
        // 색상 모노톤(0.90), 체형은 STRAIGHT와 REGULAR가 맞음(1.0), 스타일 미상(중립 0.5).
        // 0.5*0.90 + 0.3*1.0 + 0.2*0.5 = 0.85
        ClothingItem top = item(user, topDay, ClothingCategory.TOP, "#FF0000", Fit.REGULAR);
        ClothingItem bottom = item(user, bottomDay, ClothingCategory.BOTTOM, "#FF2B00", Fit.REGULAR);
        givenUserAndItems(user, List.of(top), List.of(bottom));

        List<CombinationResponse> results = service.recommendCombos(USER_ID, 10);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).score()).isEqualTo(0.85);
    }

    @Test
    void 선호_스타일에_맞는_핏일수록_점수가_높다() {
        User user = user(null, StyleCategory.STREET);
        OotdRecord topDay = ootd(101L, LocalDate.of(2026, 8, 1));
        OotdRecord bottomDayGood = ootd(102L, LocalDate.of(2026, 8, 2));
        OotdRecord bottomDayBad = ootd(103L, LocalDate.of(2026, 8, 3));

        // 체형 미상(중립 0.5)이라 스타일 핏 차이만 점수에 반영된다.
        // OVERSIZED는 STREET가 선호하는 핏 → styleFit=1.0. 0.5*0.90 + 0.3*0.5 + 0.2*1.0 = 0.80
        ClothingItem top = item(user, topDay, ClothingCategory.TOP, "#FF0000", Fit.OVERSIZED);
        ClothingItem goodBottom = item(user, bottomDayGood, ClothingCategory.BOTTOM, "#FF2B00", Fit.OVERSIZED);
        // SLIM은 STREET가 선호하지 않는 핏 → styleFit=(1.0+0.4)/2=0.7. 0.5*0.90 + 0.3*0.5 + 0.2*0.7 = 0.74
        ClothingItem badBottom = item(user, bottomDayBad, ClothingCategory.BOTTOM, "#FF2B00", Fit.SLIM);

        givenUserAndItems(user, List.of(top), List.of(goodBottom, badBottom));

        List<CombinationResponse> results = service.recommendCombos(USER_ID, 10);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).secondaryItem().id()).isEqualTo(goodBottom.getId());
        assertThat(results.get(0).score()).isEqualTo(0.80);
        assertThat(results.get(1).secondaryItem().id()).isEqualTo(badBottom.getId());
        assertThat(results.get(1).score()).isEqualTo(0.74);
    }

    @Test
    void 원피스와_아우터도_상하의와_같은_방식으로_조합_추천에_포함된다() {
        User user = user(BodyType.STRAIGHT, StyleCategory.CASUAL);
        OotdRecord dressDay = ootd(301L, LocalDate.of(2026, 8, 10));
        OotdRecord outerDay = ootd(302L, LocalDate.of(2026, 8, 11));
        ClothingItem dress = item(user, dressDay, ClothingCategory.DRESS, "#FF0000", Fit.REGULAR);
        ClothingItem outer = item(user, outerDay, ClothingCategory.OUTER, "#FF2B00", Fit.REGULAR);

        givenUserAndItems(user, List.of(), List.of());
        when(clothingItemRepository.findByUserIdAndCategoryWithOotd(eq(USER_ID), eq(ClothingCategory.DRESS)))
                .thenReturn(List.of(dress));
        when(clothingItemRepository.findByUserIdAndCategoryWithOotd(eq(USER_ID), eq(ClothingCategory.OUTER)))
                .thenReturn(List.of(outer));

        List<CombinationResponse> results = service.recommendCombos(USER_ID, 10);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).primaryItem().id()).isEqualTo(dress.getId());
        assertThat(results.get(0).secondaryItem().id()).isEqualTo(outer.getId());
        assertThat(results.get(0).primaryItem().category()).isEqualTo(ClothingCategory.DRESS);
        assertThat(results.get(0).secondaryItem().category()).isEqualTo(ClothingCategory.OUTER);
    }

    @Test
    void 같은_OOTD에_속한_원피스와_아우터_조합은_추천에서_제외한다() {
        User user = user(BodyType.STRAIGHT, StyleCategory.CASUAL);
        OotdRecord sameDay = ootd(401L, LocalDate.of(2026, 8, 12));
        ClothingItem dress = item(user, sameDay, ClothingCategory.DRESS, "#FF0000", Fit.REGULAR);
        ClothingItem outer = item(user, sameDay, ClothingCategory.OUTER, "#00FFFF", Fit.REGULAR);

        givenUserAndItems(user, List.of(), List.of());
        when(clothingItemRepository.findByUserIdAndCategoryWithOotd(eq(USER_ID), eq(ClothingCategory.DRESS)))
                .thenReturn(List.of(dress));
        when(clothingItemRepository.findByUserIdAndCategoryWithOotd(eq(USER_ID), eq(ClothingCategory.OUTER)))
                .thenReturn(List.of(outer));

        List<CombinationResponse> results = service.recommendCombos(USER_ID, 10);

        assertThat(results).isEmpty();
    }

    private void givenUserAndItems(User user, List<ClothingItem> tops, List<ClothingItem> bottoms) {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(clothingItemRepository.findByUserIdAndCategoryWithOotd(eq(USER_ID), eq(ClothingCategory.TOP)))
                .thenReturn(tops);
        when(clothingItemRepository.findByUserIdAndCategoryWithOotd(eq(USER_ID), eq(ClothingCategory.BOTTOM)))
                .thenReturn(bottoms);
        // DRESS/OUTER는 스텁하지 않으면 Mockito 기본 동작(List 반환 타입 → 빈 리스트)으로 처리돼
        // 기존 상의×하의 전용 테스트들엔 영향이 없다.
    }

    private User user(BodyType bodyType, StyleCategory preferredStyle) {
        User user = new User();
        user.setId(USER_ID);
        user.setBodyType(bodyType);
        user.setPreferredStyles(preferredStyle == null ? Set.of() : Set.of(preferredStyle));
        return user;
    }

    private OotdRecord ootd(Long id, LocalDate recordDate) {
        OotdRecord record = new OotdRecord();
        record.setId(id);
        record.setRecordDate(recordDate);
        record.setPhotoUrl("https://example.com/" + id + ".jpg");
        return record;
    }

    private ClothingItem item(User user, OotdRecord ootd, ClothingCategory category, String color, Fit fit) {
        ClothingItem item = new ClothingItem();
        item.setId(ootd.getId() * 10 + category.ordinal());
        item.setUser(user);
        item.setOotdRecord(ootd);
        item.setCategory(category);
        item.setColor(color);
        item.setFit(fit);
        return item;
    }
}
