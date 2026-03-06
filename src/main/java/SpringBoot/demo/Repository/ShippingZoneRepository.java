package SpringBoot.demo.Repository;

import SpringBoot.demo.Model.ShippingZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShippingZoneRepository extends JpaRepository<ShippingZone, Integer> {

    // Tìm phí ship theo quận/huyện và thành phố
    @Query("SELECT sz FROM ShippingZone sz WHERE sz.district = :district AND sz.city = :city AND sz.isActive = true")
    Optional<ShippingZone> findByDistrictAndCity(@Param("district") String district, @Param("city") String city);

    // Tìm phí ship theo tên khu vực
    @Query("SELECT sz FROM ShippingZone sz WHERE sz.zoneName = :zoneName AND sz.isActive = true")
    Optional<ShippingZone> findByZoneName(@Param("zoneName") String zoneName);

    // Kiểm tra khu vực tồn tại không
    boolean existsByDistrictAndCityAndIsActiveTrue(String district, String city);
}
