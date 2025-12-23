package autoservice.model;

import autoservice.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Setter
@Getter
@Table(name = "repair_orders")
@NoArgsConstructor
@AllArgsConstructor
public class RepairOrder{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.PRIVATE)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id")
    private CarServiceMaster carServiceMaster;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshop_place_id")
    private WorkshopPlace workshopPlace;

    @Setter(AccessLevel.PRIVATE)
    @Column(name = "creation_date", nullable = false)
    private LocalDate creationDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "total_price")
    private Float totalPrice;

    public RepairOrder(LocalDate creationDate, LocalDate startDate, LocalDate endDate, String description) {
        this.creationDate = creationDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.status = OrderStatus.CREATED;
    }

    public void cancel() {
        status = OrderStatus.CANCELLED;
    }

    public void close() {
        status = OrderStatus.CLOSED;
    }

    @Override
    public String toString() {
        return "model.Order{" + " uuid=" + id + ", status=" + status +
                "\nDescription=" + description + "\nPlace=" + workshopPlace +
                ", carServiceMaster=" + carServiceMaster +
                ", starDate=" + startDate +
                ", endDate=" + endDate + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepairOrder order = (RepairOrder) o;
        return id.equals(order.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}