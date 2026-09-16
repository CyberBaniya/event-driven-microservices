package com.cyboul.eda.productservice;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
@Data
public class Product {
    @Id
    private String uuid;
    private String name;
    private double price;
    private int stock;
}