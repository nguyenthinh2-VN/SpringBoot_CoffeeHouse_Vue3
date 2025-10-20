package SpringBoot.demo.Repository;

import SpringBoot.demo.Model.Topping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ToppingRepository extends JpaRepository<Topping, Integer> {
    
    /**
     * Lấy tất cả topping
     */
    List<Topping> findAll();
}
