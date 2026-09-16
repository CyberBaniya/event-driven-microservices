package com.cyboul.eda.common.dto;

public record ProductDTO(
        String id,
        String name,
        double price,
        int stock
) {}
