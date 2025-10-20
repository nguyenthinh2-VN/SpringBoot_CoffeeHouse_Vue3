package SpringBoot.demo.Repository;

import SpringBoot.demo.Model.IceOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IceOptionRepository extends JpaRepository<IceOption, Integer> {
    
    /**
     * Lấy tất cả tùy chọn đá
     */
    List<IceOption> findAll();
}
