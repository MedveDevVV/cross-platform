package autoservice.repository;

import autoservice.model.CarServiceMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CarServiceMasterRepository extends JpaRepository<CarServiceMaster, UUID> {

    Optional<CarServiceMaster> findByFullName(String fullName);

    List<CarServiceMaster> findAllByOrderByFullNameAsc();

    List<CarServiceMaster> findByFullNameContainingIgnoreCase(String name);

    @Query("SELECT m FROM CarServiceMaster m WHERE m.fullName LIKE %:namePart%")
    List<CarServiceMaster> findByFullNameContaining(@Param("namePart") String namePart);
}