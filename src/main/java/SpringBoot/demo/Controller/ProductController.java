package SpringBoot.demo.Controller;

import SpringBoot.demo.Model.Product;
import SpringBoot.demo.Model.PaginationResponse;
import SpringBoot.demo.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/products")
    public PaginationResponse<Product> getAllProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        
        // If no pagination parameters provided, return all products with pagination info
        if (page == null) page = 1;
        if (size == null) size = 10;
        
        // Convert page from 1-based to 0-based for Spring Data
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Product> productPage = productRepository.findAll(pageable);
        
        // Create pagination info
        PaginationResponse.PaginationInfo paginationInfo = new PaginationResponse.PaginationInfo(
                productPage.getTotalElements(),
                productPage.getNumberOfElements(),
                page,
                productPage.getTotalPages(),
                0 // updateToday - you can implement this based on your business logic
        );
        
        return new PaginationResponse<>(productPage.getContent(), paginationInfo);
    }
}
