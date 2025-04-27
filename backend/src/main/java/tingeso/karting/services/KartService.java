package tingeso.karting.services;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KartService {

    private final List<String> kartIds = List.of(
        "K001", "K002", "K003", "K004", "K005",
        "K006", "K007", "K008", "K009", "K010",
        "K011", "K012", "K013", "K014", "K015");

    public List<String> getAllKartIds() {
        return kartIds;
    }
}