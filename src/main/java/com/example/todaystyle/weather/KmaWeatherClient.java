package com.example.todaystyle.weather;

import tools.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 기상청 단기예보 조회서비스 호출. 초단기실황(현재 기온·강수형태)과
 * 단기예보(오늘 최저/최고·하늘상태·강수확률)를 각각 조회한다.
 */
@Component
public class KmaWeatherClient {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    /** 단기예보 발표 시각(정시). */
    private static final int[] FORECAST_BASE_HOURS = {2, 5, 8, 11, 14, 17, 20, 23};

    private final RestClient kmaRestClient;
    private final WeatherProperties properties;

    public KmaWeatherClient(RestClient kmaRestClient, WeatherProperties properties) {
        this.kmaRestClient = kmaRestClient;
        this.properties = properties;
    }

    /** 초단기실황: 현재 기온(T1H)과 강수형태(PTY). */
    public CurrentWeather getCurrent(int nx, int ny) {
        // 실황은 매시 정시 발표, 약 40분 뒤 제공되므로 40분 미만이면 이전 시각을 사용한다.
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime base = now.getMinute() < 40 ? now.minusHours(1) : now;
        JsonNode items = call("/getUltraSrtNcst", nx, ny,
                base.format(DATE), String.format("%02d00", base.getHour()));

        Double temperature = null;
        Integer pty = null;
        for (JsonNode item : items) {
            String category = item.path("category").asString();
            String value = item.path("obsrValue").asString(null);
            if ("T1H".equals(category) && value != null) {
                temperature = Double.valueOf(value);
            } else if ("PTY".equals(category) && value != null) {
                pty = Integer.valueOf(value);
            }
        }
        if (temperature == null) {
            throw new WeatherUnavailableException("기상청 실황 응답에 기온(T1H)이 없습니다.");
        }
        return new CurrentWeather(temperature, pty);
    }

    /** 단기예보: 오늘의 최저(TMN)/최고(TMX) 기온, 현재 시각 이후 가장 가까운 하늘상태(SKY)·강수확률(POP). */
    public TodayForecast getTodayForecast(int nx, int ny) {
        LocalDateTime announce = latestForecastAnnouncement(LocalDateTime.now());
        JsonNode items = call("/getVilageFcst", nx, ny,
                announce.format(DATE), String.format("%02d00", announce.getHour()));

        String today = LocalDate.now().format(DATE);
        String nowHhmm = String.format("%02d00", LocalDateTime.now().getHour());

        Double min = null;
        Double max = null;
        Integer sky = null;
        Integer pop = null;
        String bestFcstTime = null; // SKY/POP는 현재 시각 이후 가장 가까운 예보값을 사용

        for (JsonNode item : items) {
            String category = item.path("category").asString();
            String fcstDate = item.path("fcstDate").asString();
            String fcstTime = item.path("fcstTime").asString();
            String value = item.path("fcstValue").asString(null);
            if (value == null || !today.equals(fcstDate)) {
                continue;
            }
            switch (category) {
                case "TMN" -> min = Double.valueOf(value);
                case "TMX" -> max = Double.valueOf(value);
                case "SKY", "POP" -> {
                    if (fcstTime.compareTo(nowHhmm) >= 0
                            && (bestFcstTime == null || fcstTime.compareTo(bestFcstTime) < 0)) {
                        // 가장 가까운 예보 시각을 갱신하되, SKY/POP는 아래에서 해당 시각 값으로 다시 채운다
                        bestFcstTime = fcstTime;
                    }
                }
                default -> {
                }
            }
        }
        // 확정된 bestFcstTime의 SKY/POP 값을 추출
        if (bestFcstTime != null) {
            for (JsonNode item : items) {
                if (!today.equals(item.path("fcstDate").asString())
                        || !bestFcstTime.equals(item.path("fcstTime").asString())) {
                    continue;
                }
                String category = item.path("category").asString();
                String value = item.path("fcstValue").asString(null);
                if (value == null) {
                    continue;
                }
                if ("SKY".equals(category)) {
                    sky = Integer.valueOf(value);
                } else if ("POP".equals(category)) {
                    pop = Integer.valueOf(value);
                }
            }
        }
        return new TodayForecast(min, max, sky, pop);
    }

    /** 현재 시각 기준 가장 최근 단기예보 발표 시각(제공 지연 감안 10분). */
    private LocalDateTime latestForecastAnnouncement(LocalDateTime now) {
        LocalDateTime t = now.minusMinutes(10);
        for (int i = FORECAST_BASE_HOURS.length - 1; i >= 0; i--) {
            if (t.getHour() >= FORECAST_BASE_HOURS[i]) {
                return t.withHour(FORECAST_BASE_HOURS[i]).withMinute(0);
            }
        }
        // 02시 이전이면 전날 23시 발표를 사용
        return t.minusDays(1).withHour(23).withMinute(0);
    }

    private JsonNode call(String path, int nx, int ny, String baseDate, String baseTime) {
        if (!properties.isConfigured()) {
            throw new WeatherUnavailableException(
                    "기상청 서비스키가 설정되지 않았습니다. KMA_SERVICE_KEY 를 설정하세요.");
        }
        JsonNode root;
        try {
            root = kmaRestClient.get()
                    .uri(uri -> uri.path(path)
                            .queryParam("serviceKey", properties.serviceKey())
                            .queryParam("dataType", "JSON")
                            .queryParam("numOfRows", "1000")
                            .queryParam("pageNo", "1")
                            .queryParam("base_date", baseDate)
                            .queryParam("base_time", baseTime)
                            .queryParam("nx", nx)
                            .queryParam("ny", ny)
                            .build())
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RuntimeException e) {
            throw new WeatherUnavailableException("기상청 API 호출에 실패했습니다.", e);
        }
        if (root == null) {
            throw new WeatherUnavailableException("기상청 API 응답이 비어 있습니다.");
        }
        String resultCode = root.path("response").path("header").path("resultCode").asString("");
        if (!"00".equals(resultCode)) {
            String msg = root.path("response").path("header").path("resultMsg").asString("알 수 없는 오류");
            throw new WeatherUnavailableException("기상청 API 오류(" + resultCode + "): " + msg);
        }
        return root.path("response").path("body").path("items").path("item");
    }

    public record CurrentWeather(double temperature, Integer precipitationTypeCode) {
    }

    public record TodayForecast(Double minTemp, Double maxTemp, Integer skyCode, Integer precipProbability) {
    }
}
