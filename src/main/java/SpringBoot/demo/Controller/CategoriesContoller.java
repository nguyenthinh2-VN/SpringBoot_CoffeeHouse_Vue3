package SpringBoot.demo.Controller;

import SpringBoot.demo.DTO.ApiResponse;
import SpringBoot.demo.Model.Category;
import SpringBoot.demo.Repository.Category.CategoriesRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/categories")
@Tag(name = "Categories", description = "APIs quản lý danh mục sản phẩm")
public class CategoriesContoller {

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Operation(summary = "Lấy danh sách categories", description = "Lấy tất cả các danh mục sản phẩm")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        try {
            List<Category> categories = categoriesRepository.findAll();
            return ResponseEntity.ok(ApiResponse.success(categories));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách categories: " + e.getMessage()));
        }
    }
}
