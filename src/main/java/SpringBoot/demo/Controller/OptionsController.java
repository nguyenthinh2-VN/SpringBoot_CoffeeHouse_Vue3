package SpringBoot.demo.Controller;

import SpringBoot.demo.DTO.ApiResponse;
import SpringBoot.demo.Model.IceOption;
import SpringBoot.demo.Model.Size;
import SpringBoot.demo.Model.Topping;
import SpringBoot.demo.Repository.Option.IceOptionRepository;
import SpringBoot.demo.Repository.Option.SizeRepository;
import SpringBoot.demo.Repository.Option.ToppingRepository;
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
@RequestMapping("/options")
@Tag(name = "Options", description = "APIs quản lý các tùy chọn sản phẩm (size, ice, topping)")
public class OptionsController {

    @Autowired
    private IceOptionRepository iceOptionRepository;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private ToppingRepository toppingRepository;

    // GET /options/ice - Lấy danh sách ice options
    @Operation(summary = "Lấy danh sách ice options", description = "Lấy tất cả các tùy chọn đá")
    @GetMapping("/ice")
    public ResponseEntity<ApiResponse<List<IceOption>>> getAllIceOptions() {
        try {
            List<IceOption> iceOptions = iceOptionRepository.findAll();
            return ResponseEntity.ok(ApiResponse.success(iceOptions));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách ice options: " + e.getMessage()));
        }
    }

    // GET /options/sizes - Lấy danh sách sizes
    @Operation(summary = "Lấy danh sách sizes", description = "Lấy tất cả các size sản phẩm")
    @GetMapping("/sizes")
    public ResponseEntity<ApiResponse<List<Size>>> getAllSizes() {
        try {
            List<Size> sizes = sizeRepository.findAll();
            return ResponseEntity.ok(ApiResponse.success(sizes));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách sizes: " + e.getMessage()));
        }
    }

    // GET /options/toppings - Lấy danh sách toppings
    @Operation(summary = "Lấy danh sách toppings", description = "Lấy tất cả các topping")
    @GetMapping("/toppings")
    public ResponseEntity<ApiResponse<List<Topping>>> getAllToppings() {
        try {
            List<Topping> toppings = toppingRepository.findAll();
            return ResponseEntity.ok(ApiResponse.success(toppings));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách toppings: " + e.getMessage()));
        }
    }
}
