package com.example.todaystyle.weather;

/**
 * 기온·강수 기반의 간단한 착장 가이드. 색상 조합과 마찬가지로 외부 데이터 없이
 * 규칙으로 시작하며, 흔히 쓰이는 기온별 옷차림 구간을 사용한다.
 */
public final class OutfitTip {

    private OutfitTip() {
    }

    public static String forWeather(double temperature, Integer precipitationTypeCode) {
        String base = byTemperature(temperature);
        String layering = layeringNote(temperature);
        StringBuilder tip = new StringBuilder(base);
        if (layering != null) {
            tip.append(' ').append(layering);
        }
        if (WeatherCodes.hasPrecipitation(precipitationTypeCode)) {
            tip.append(" 오늘은 ")
                    .append(WeatherCodes.precipitation(precipitationTypeCode))
                    .append(" 소식이 있으니 우산과 방수 아우터를 챙기세요.");
        }
        return tip.toString();
    }

    private static String byTemperature(double t) {
        if (t >= 28) {
            return "민소매·반팔·반바지·린넨 소재로 시원하게 입기 좋은 날씨예요.";
        }
        if (t >= 23) {
            return "반팔·얇은 셔츠·반바지·면바지가 적당해요.";
        }
        if (t >= 20) {
            return "얇은 가디건·긴팔·면바지·청바지가 어울려요.";
        }
        if (t >= 17) {
            return "얇은 니트·맨투맨·가디건·청바지가 좋아요.";
        }
        if (t >= 12) {
            return "자켓·가디건·야상에 청바지·면바지를 매치하세요.";
        }
        if (t >= 9) {
            return "트렌치코트·야상·자켓에 니트를 더하면 좋아요.";
        }
        if (t >= 5) {
            return "코트·가죽자켓에 히트텍·니트로 보온하세요.";
        }
        return "패딩·두꺼운 코트에 목도리·기모 제품으로 단단히 챙겨 입으세요.";
    }

    private static String layeringNote(double t) {
        if (t >= 12 && t < 20) {
            return "일교차가 있을 수 있으니 겉옷으로 레이어링하는 걸 추천해요.";
        }
        return null;
    }
}
