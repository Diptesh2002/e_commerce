package com.zefrotech.ecom.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prod_id")
    private Long prodId;

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 100, message = "Name must be 3-100 characters")
    @Column(name = "prod_name")
    @NotNull(message = "Product name cannot be null")
    private String prodName;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(name = "prod_price")
    private BigDecimal prodPrice;

    @NotBlank(message = "Description is required")
    @Column(name = "prod_description", length = 500)
    private String prodDescription;

    @Column(name = "img_url", length = 2048)
    private String imgUrl;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Column(name = "stock_quantity")
    private int stockQuantity;

    private boolean available;
}
