package autoservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "car_service_masters")
@Getter
@Setter
@NoArgsConstructor
public class CarServiceMaster extends Person {

    /**@param fullName Полное имя в формате "Фамилия Имя Отчество"*/
    public CarServiceMaster(String fullName, LocalDate dateOfBirth) {
        super(fullName, dateOfBirth);
    }

    /**@param fullName Полное имя в формате "Фамилия Имя Отчество"*/
    public CarServiceMaster(UUID id, String fullName, LocalDate dateOfBirth) {
        super(id, fullName, dateOfBirth);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return getId().equals(((CarServiceMaster) o).getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

}
