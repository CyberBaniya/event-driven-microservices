package com.cyboul.eda.productservice;

import com.cyboul.eda.common.dto.ProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository repo;

    @GetMapping
    public Flux<ProductDTO> all() {
        return repo.findAll()
                .map(this::toDto);
    }

    @GetMapping("/{id}")
    public Mono<ProductDTO> byId(@PathVariable String id) {
        return repo.findById(id)
                .map(this::toDto)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + id)));
    }

    private ProductDTO toDto(Product product) {
        return new ProductDTO(
                product.getUuid(),
                product.getName(),
                product.getPrice(),
                product.getStock()
        );
    }
}