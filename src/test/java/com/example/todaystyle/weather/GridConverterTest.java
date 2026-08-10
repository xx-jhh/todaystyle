package com.example.todaystyle.weather;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GridConverterTest {

    @Test
    void 서울시청_위경도를_기상청_격자로_변환한다() {
        GridConverter.Grid grid = GridConverter.toGrid(37.5665, 126.9780);

        assertThat(grid.nx()).isEqualTo(60);
        assertThat(grid.ny()).isEqualTo(127);
    }

    @Test
    void 부산시청_위경도를_기상청_격자로_변환한다() {
        // 부산 시청 약 35.1798, 129.0750 → nx=98, ny=76
        GridConverter.Grid grid = GridConverter.toGrid(35.1798, 129.0750);

        assertThat(grid.nx()).isEqualTo(98);
        assertThat(grid.ny()).isEqualTo(76);
    }
}
