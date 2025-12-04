package autoservice.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CarServiceMasterDTO(
        UUID id,
        String fullName,
        LocalDate dateOfBirth
) {}