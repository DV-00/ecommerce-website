package com.ecommerce.productservice.models;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;


@Data
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"title", "category_id"})})
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;

    private double price;

    private String description;

    private String image;

    @ManyToOne
    private Category category;

    @Column(nullable = false)
    private int quantity = 0;

}
