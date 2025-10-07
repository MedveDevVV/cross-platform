package autoservice.model;

import autoservice.enums.OrderStatus;

import java.time.LocalDate;
import java.util.UUID;

public interface Order extends Identifiable{
    String getDescription();
    OrderStatus getStatus();
    void setStartDate(LocalDate startDate);
    void setEndDate(LocalDate endDate);
    void setTotalPrice(Float totalPrice);
    void cancel();
    void closed();
    void assignPerson(Person person);
    LocalDate getStartDate();
    LocalDate getEndDate();
    Float getTotalPrice();
    Person getAssignPerson();
    LocalDate getCreationDate();
}
