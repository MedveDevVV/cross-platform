package autoservice.model;

import java.time.LocalDate;
import java.util.UUID;

public abstract class Person implements Identifiable {
    private final UUID id;
    private String fullName;

    public Person(String surname, String name, String patronymic, LocalDate dateOfBirth) {
        this.id = UUID.randomUUID();
        this.fullName = String.join(" ", surname, name, patronymic);
        this.dateOfBirth = dateOfBirth;
    }

    public Person(UUID id, String fullName, LocalDate dateOfBirth) {
        this.id = id;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    private LocalDate dateOfBirth;

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getFullName(){
        return fullName;
    }

    @Override
    public UUID getId(){
        return id;
    }
}
