package com.example.todaystyle.recommendation;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.todaystyle.recommendation.ColorHarmony.Result;
import org.junit.jupiter.api.Test;

class ColorHarmonyTest {

    @Test
    void 색상차이가_15도_이하면_모노톤으로_판정한다() {
        Result result = ColorHarmony.evaluate("#FF0000", "#FF2B00"); // hue 0 vs 10

        assertThat(result.label()).isEqualTo("모노톤");
        assertThat(result.score()).isEqualTo(0.90);
    }

    @Test
    void 색상차이가_45도_이하면_유사색으로_판정한다() {
        Result result = ColorHarmony.evaluate("#FF0000", "#FF8000"); // hue 0 vs 30

        assertThat(result.label()).isEqualTo("유사색");
        assertThat(result.score()).isEqualTo(0.80);
    }

    @Test
    void 색상차이가_180도에_가까우면_보색_대비로_판정한다() {
        Result result = ColorHarmony.evaluate("#FF0000", "#00FFFF"); // hue 0 vs 180

        assertThat(result.label()).isEqualTo("보색 대비");
        assertThat(result.score()).isEqualTo(0.85);
    }

    @Test
    void 색상차이가_120도에_가까우면_삼색_대비로_판정한다() {
        Result result = ColorHarmony.evaluate("#FF0000", "#00FF00"); // hue 0 vs 120

        assertThat(result.label()).isEqualTo("삼색 대비");
        assertThat(result.score()).isEqualTo(0.78);
    }

    @Test
    void 애매한_각도차는_대비_애매로_감점된다() {
        Result result = ColorHarmony.evaluate("#FF0000", "#EAFF00"); // hue 0 vs 65

        assertThat(result.label()).isEqualTo("대비 애매");
        assertThat(result.score()).isEqualTo(0.45);
    }

    @Test
    void 흰색처럼_채도가_낮으면_무채색_매치로_판정한다() {
        Result result = ColorHarmony.evaluate("#FFFFFF", "#FF0000");

        assertThat(result.label()).isEqualTo("무채색 매치");
        assertThat(result.score()).isEqualTo(0.85);
    }

    @Test
    void 검은색처럼_명도가_낮으면_무채색_매치로_판정한다() {
        Result result = ColorHarmony.evaluate("#000000", "#FF0000");

        assertThat(result.label()).isEqualTo("무채색 매치");
        assertThat(result.score()).isEqualTo(0.85);
    }

    @Test
    void 색상값이_null이면_색상_정보_없음으로_중립처리한다() {
        Result result = ColorHarmony.evaluate(null, "#FF0000");

        assertThat(result.label()).isEqualTo("색상 정보 없음");
        assertThat(result.score()).isEqualTo(0.5);
    }

    @Test
    void 유효하지_않은_hex_형식이면_색상_정보_없음으로_중립처리한다() {
        Result result = ColorHarmony.evaluate("#FFF", "#FF0000");

        assertThat(result.label()).isEqualTo("색상 정보 없음");
        assertThat(result.score()).isEqualTo(0.5);
    }

    @Test
    void 샵_기호가_없어도_동일하게_판정한다() {
        Result withHash = ColorHarmony.evaluate("#FF0000", "#FF2B00");
        Result withoutHash = ColorHarmony.evaluate("FF0000", "FF2B00");

        assertThat(withoutHash).isEqualTo(withHash);
    }
}
