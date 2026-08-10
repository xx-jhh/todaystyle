package com.example.todaystyle.weather;

/**
 * 위경도(lat/lon)를 기상청 단기예보 격자 좌표(nx, ny)로 변환한다.
 * 기상청이 공개한 Lambert Conformal Conic 투영 파라미터를 그대로 사용한다.
 * (예: 서울 시청 37.5665, 126.9780 → nx=60, ny=127)
 */
public final class GridConverter {

    private static final double RE = 6371.00877; // 지구 반경(km)
    private static final double GRID = 5.0;       // 격자 간격(km)
    private static final double SLAT1 = 30.0;     // 표준 위도1
    private static final double SLAT2 = 60.0;     // 표준 위도2
    private static final double OLON = 126.0;     // 기준점 경도
    private static final double OLAT = 38.0;      // 기준점 위도
    private static final double XO = 43;          // 기준점 X좌표(격자)
    private static final double YO = 136;         // 기준점 Y좌표(격자)
    private static final double DEGRAD = Math.PI / 180.0;

    public record Grid(int nx, int ny) {
    }

    private GridConverter() {
    }

    public static Grid toGrid(double lat, double lon) {
        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);

        double ra = Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        double theta = lon * DEGRAD - olon;
        if (theta > Math.PI) {
            theta -= 2.0 * Math.PI;
        }
        if (theta < -Math.PI) {
            theta += 2.0 * Math.PI;
        }
        theta *= sn;

        int nx = (int) Math.floor(ra * Math.sin(theta) + XO + 0.5);
        int ny = (int) Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);
        return new Grid(nx, ny);
    }
}
