package SpringBoot.demo.Controller;

import SpringBoot.demo.Model.Category;
import SpringBoot.demo.Repository.CategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
public class CategoriesContoller {

    @Autowired
    private CategoriesRepository categoriesRepository;

    @GetMapping("/categories")
    public List<Category> getAllCategories() {
        return categoriesRepository.findAll();
    }
}
