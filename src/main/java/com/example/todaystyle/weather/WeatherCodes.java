package com.example.todaystyle.weather;

/**
 * 기상청 응답의 코드값을 사람이 읽을 수 있는 라벨로 변환한다.
 */
public final class WeatherCodes {

    private WeatherCodes() {
    }

    /** 하늘상태(SKY): 1 맑음, 3 구름많음, 4 흐림. */
    public static String sky(Integer code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case 1 -> "맑음";
            case 3 -> "구름많음";
            case 4 -> "흐림";
            default -> "정보없음";
        };
    }

    /** 강수형태(PTY): 0 없음, 1 비, 2 비/눈, 3 눈, 4 소나기, 5 빗방울, 6 빗방울눈날림, 7 눈날림. */
    public static String precipitation(Integer code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case 0 -> "없음";
            case 1 -> "비";
            case 2 -> "비/눈";
            case 3 -> "눈";
            case 4 -> "소나기";
            case 5 -> "빗방울";
            case 6 -> "빗방울눈날림";
            case 7 -> "눈날림";
            default -> "정보없음";
        };
    }

    /** 강수형태 코드가 실제 강수(비/눈 등)를 의미하는지. */
    public static boolean hasPrecipitation(Integer ptyCode) {
        return ptyCode != null && ptyCode != 0;
    }
}
