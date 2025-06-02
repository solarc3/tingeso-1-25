package tingeso.tariffsservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConflictDto {
    private String kartId;
    private OffsetDateTime start;
    private OffsetDateTime end;
}
