package tingeso.karting.services;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KartServiceTest {

    private final KartService kartService = new KartService();

    @Test
    @DisplayName("getAllKartIds devuelve una lista no nula de tamaño 15")
    void testGetAllKartIdsNotNullAndSize() {
        List<String> ids = kartService.getAllKartIds();
        assertThat(ids)
            .isNotNull()
            .hasSize(15);
    }

    @Test
    @DisplayName("getAllKartIds devuelve los IDs esperados en orden")
    void testGetAllKartIdsContentAndOrder() {
        List<String> ids = kartService.getAllKartIds();
        assertThat(ids).containsExactly(
            "K001", "K002", "K003", "K004", "K005",
            "K006", "K007", "K008", "K009", "K010",
            "K011", "K012", "K013", "K014", "K015"
                                       );
    }

    @Test
    @DisplayName("La lista retornada es inmodificable")
    void testReturnedListUnmodifiable() {
        List<String> ids = kartService.getAllKartIds();
        assertThatExceptionOfType(UnsupportedOperationException.class)
            .isThrownBy(() -> ids.add("K016"));
    }
}
