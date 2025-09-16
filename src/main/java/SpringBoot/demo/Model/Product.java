package SpringBoot.demo.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String tensp;
    private double gia;
    private int category_id;
    private String hinh;

    // Constructors
    public Product() {
    }

    public Product(int id, String tensp, double gia, int category_id, String hinh) {
        this.id = id;
        this.tensp = tensp;
        this.gia = gia;
        this.category_id = category_id;
        this.hinh = hinh;
    }


}
