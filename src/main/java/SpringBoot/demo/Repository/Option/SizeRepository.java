package SpringBoot.demo.Repository.Option;

import SpringBoot.demo.Model.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SizeRepository extends JpaRepository<Size, Integer> {
    
    /**
     * Lấy tất cả size
     */
    List<Size> findAll();
}
