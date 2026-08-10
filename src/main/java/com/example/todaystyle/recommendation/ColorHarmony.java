package com.example.todaystyle.recommendation;

import java.awt.Color;

/**
 * 두 색상의 어울림을 색상 이론(색상환 각도)으로 점수화한다. 별도 데이터셋 없이
 * hex 색상값만으로 계산하며, 데이터가 쌓이면 임베딩 기반으로 고도화한다.
 *
 * 규칙(대략): 모노톤(≈동일) · 유사색(±45°) · 보색(≈180°) · 삼색(≈120°)에 가점,
 * 애매한 각도는 감점, 무채색(낮은 채도)은 무엇과도 무난.
 */
public final class ColorHarmony {

    /** 채도가 이 값보다 낮으면 무채색(화이트/그레이/블랙/베이지 계열)으로 간주. */
    private static final float NEUTRAL_SATURATION = 0.15f;
    /** 명도가 이 값보다 낮으면(거의 검정) 무채색으로 간주. */
    private static final float NEUTRAL_BRIGHTNESS = 0.12f;

    public record Result(double score, String label) {
    }

    private ColorHarmony() {
    }

    public static Result evaluate(String hexA, String hexB) {
        float[] a = toHsb(hexA);
        float[] b = toHsb(hexB);
        if (a == null || b == null) {
            return new Result(0.5, "색상 정보 없음");
        }
        if (isNeutral(a) || isNeutral(b)) {
            return new Result(0.85, "무채색 매치");
        }

        double diff = hueDifferenceDegrees(a[0], b[0]);
        if (diff <= 15) {
            return new Result(0.90, "모노톤");
        }
        if (diff <= 45) {
            return new Result(0.80, "유사색");
        }
        if (Math.abs(diff - 180) <= 20) {
            return new Result(0.85, "보색 대비");
        }
        if (Math.abs(diff - 120) <= 20) {
            return new Result(0.78, "삼색 대비");
        }
        return new Result(0.45, "대비 애매");
    }

    /** hex("#RRGGBB" 또는 "RRGGBB") → HSB[hue0..1, sat0..1, bri0..1]. 유효하지 않으면 null. */
    private static float[] toHsb(String hex) {
        if (hex == null) {
            return null;
        }
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        if (h.length() != 6) {
            return null;
        }
        try {
            int r = Integer.parseInt(h.substring(0, 2), 16);
            int g = Integer.parseInt(h.substring(2, 4), 16);
            int b = Integer.parseInt(h.substring(4, 6), 16);
            return Color.RGBtoHSB(r, g, b, null);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isNeutral(float[] hsb) {
        return hsb[1] < NEUTRAL_SATURATION || hsb[2] < NEUTRAL_BRIGHTNESS;
    }

    /** 두 색상(0..1로 표현된 hue)의 색상환상 최소 각도차(0..180도). */
    private static double hueDifferenceDegrees(float hueA, float hueB) {
        double d = Math.abs(hueA - hueB) * 360.0;
        return d > 180 ? 360 - d : d;
    }
}
