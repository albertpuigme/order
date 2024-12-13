package net.apuig.product;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import net.apuig.product.dto.CategoryDto;
import net.apuig.product.dto.ProductDto;

@RestController
public class ProductController
{
    private final ProductService productService;

    public ProductController(ProductService productService)
    {
        this.productService = productService;
    }

    public enum CategorySort
    {
        id, name
    };

    /** Find categories. Optionally starting at some parent category */
    @GetMapping({"/categories", "/categories/{fromParentCategory}"})
    public ResponseEntity<PagedModel<CategoryDto>> listCategories(
        @PathVariable(required = false) final Long fromParentCategory,
        @RequestParam(required = false) final String nameLike,
        @RequestParam(defaultValue = "0") final int pageNo,
        @RequestParam(defaultValue = "10") @Valid @Max(50) final int pageSize,
        @RequestParam(defaultValue = "name") final CategorySort sortBy,
        @RequestParam(defaultValue = "true") final boolean asc)
    {
        return ResponseEntity.ok(
            new PagedModel<>(productService.listCategories(nameLike, fromParentCategory, PageRequest
                .of(pageNo, pageSize, asc ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy.name()))));
    }

    public enum ProductSort
    {
        id, name, price
    };

    /** Find products. Optionally starting at some parent category */
    @GetMapping({"/products", "/categories/{fromParentCategory}/products"})
    public ResponseEntity<PagedModel<ProductDto>> listProducts(
        @PathVariable(required = false) final Long fromParentCategory,
        @RequestParam(required = false) final String nameLike,
        @RequestParam(defaultValue = "0") final int pageNo,
        @RequestParam(defaultValue = "10") @Valid @Max(50) final int pageSize,
        @RequestParam(defaultValue = "name") final ProductSort sortBy,
        @RequestParam(defaultValue = "true") final boolean asc)
    {
        return ResponseEntity.ok(
            new PagedModel<>(productService.listProducts(nameLike, fromParentCategory, PageRequest
                .of(pageNo, pageSize, asc ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy.name()))));
    }
    
    
    // TODO missing attendant endpoints to check current stock of products
}
