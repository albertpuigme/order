package net.apuig.product;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Nullable;
import net.apuig.LikeUtil;
import net.apuig.product.dto.CategoryDto;
import net.apuig.product.dto.ProductDto;

@Service
@Transactional(readOnly = true)
public class ProductService
{
    private final CategoryRepository categoryRepository;

    private final ProductRepository productRepository;

    public ProductService(CategoryRepository categoryRepository,
        ProductRepository productRepository)
    {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        // TODO we could Memorize the root category, it can't be changed
    }

    public Page<CategoryDto> listCategories(@Nullable String nameLike,
        @Nullable Long fromParentCategory, Pageable page)
    {
        return categoryRepository
            .listCategories(LikeUtil.like(nameLike), orRootCategory(fromParentCategory), page)
            .map(ProductService::toDto);
    }

    public Page<ProductDto> listProducts(@Nullable String nameLike,
        @Nullable Long fromParentCategory, Pageable page)
    {
        if (fromParentCategory != null)
        {
            // TODO we could improve with a single query to product table 
            Set<Long> categories =
                categoryRepository.listCategoriesIdsDownCurrent(fromParentCategory);
            return productRepository.listProducts(LikeUtil.like(nameLike), categories, page)
                .map(ProductService::toDto);
        }

        return productRepository.listProducts(LikeUtil.like(nameLike), page)
            .map(ProductService::toDto);
    }

    private Long orRootCategory(Long fromParentCategory)
    {
        return fromParentCategory != null ? fromParentCategory
            : categoryRepository.getRootCategoryId();
    }

    private static CategoryDto toDto(CategoryProjection c)
    {
        return new CategoryDto(c.getId(), c.getName(), c.getParentId(), c.getParentName());
    }

    private static ProductDto toDto(Product p)
    {
        // PLEASE NOTE: repository use an EntityGraph to avoid additional fetch
        Category c = p.getCategory();
        return new ProductDto(p.getId(), p.getName(), p.getPrice(), p.getCurrency(),
            new CategoryDto(c.getId(), c.getName(), c.getParent().getId(), c.getParent().getName()),
            p.getImageUrl());
    }

}
