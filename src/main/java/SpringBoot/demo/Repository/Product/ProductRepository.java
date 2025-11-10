package SpringBoot.demo.Repository.Product;

import SpringBoot.demo.Model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository cho Product với các query tìm kiếm và lọc
 * Tuân thủ nguyên lý Single Responsibility: chỉ quản lý truy vấn database
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    
    Page<Product> findAll(Pageable pageable);
    
    /**
     * Tìm kiếm full-text theo tên sản phẩm
     * Sử dụng MySQL MATCH AGAINST cho full-text search hiệu quả
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(p.tensp) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Tìm kiếm theo danh mục
     */
    @Query("SELECT p FROM Product p WHERE p.category_id = :categoryId")
    Page<Product> findByCategory_id(@Param("categoryId") Integer categoryId, Pageable pageable);
    
    /**
     * Tìm kiếm theo khoảng giá
     */
    @Query("SELECT p FROM Product p WHERE p.gia BETWEEN :minPrice AND :maxPrice")
    Page<Product> findByPriceRange(@Param("minPrice") Double minPrice, 
                                   @Param("maxPrice") Double maxPrice, 
                                   Pageable pageable);
    
    /**
     * Tìm kiếm kết hợp: từ khóa + danh mục
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(p.tensp) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR p.category_id = :categoryId)")
    Page<Product> findByKeywordAndCategory(@Param("keyword") String keyword,
                                          @Param("categoryId") Integer categoryId,
                                          Pageable pageable);
    
    /**
     * Tìm kiếm kết hợp: từ khóa + khoảng giá
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(p.tensp) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:minPrice IS NULL OR p.gia >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.gia <= :maxPrice)")
    Page<Product> findByKeywordAndPriceRange(@Param("keyword") String keyword,
                                            @Param("minPrice") Double minPrice,
                                            @Param("maxPrice") Double maxPrice,
                                            Pageable pageable);
    
    /**
     * Tìm kiếm kết hợp: danh mục + khoảng giá
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:categoryId IS NULL OR p.category_id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.gia >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.gia <= :maxPrice)")
    Page<Product> findByCategoryAndPriceRange(@Param("categoryId") Integer categoryId,
                                             @Param("minPrice") Double minPrice,
                                             @Param("maxPrice") Double maxPrice,
                                             Pageable pageable);
    
    /**
     * Tìm kiếm tổng hợp với tất cả tiêu chí
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(p.tensp) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR p.category_id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.gia >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.gia <= :maxPrice)")
    Page<Product> findByAllCriteria(@Param("keyword") String keyword,
                                   @Param("categoryId") Integer categoryId,
                                   @Param("minPrice") Double minPrice,
                                   @Param("maxPrice") Double maxPrice,
                                   Pageable pageable);
    
    /**
     * Đếm số lượng sản phẩm theo danh mục
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category_id = :categoryId")
    long countByCategory_id(@Param("categoryId") Integer categoryId);
    
    /**
     * Tìm sản phẩm mới nhất (theo ID)
     */
    Page<Product> findAllByOrderByIdDesc(Pageable pageable);
}
