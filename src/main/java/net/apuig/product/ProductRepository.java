package net.apuig.product;

import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import jakarta.annotation.Nullable;

public interface ProductRepository extends JpaRepository<Product, Long>
{
    Product findByName(String name);

    @Query("""
    SELECT p FROM Product p 
    WHERE (:nameLike IS NULL OR UPPER(p.name) LIKE :nameLike) AND available > 0
    """)
    @EntityGraph(attributePaths = {"category", "category.parent"})
    Page<Product> listProducts(@Nullable String nameLike, Pageable page);

    @Query("""
    SELECT p FROM Product p 
    WHERE (:nameLike IS NULL OR UPPER(p.name) LIKE :nameLike) AND available > 0
    AND p.category.id IN (:categories)
    """)
    @EntityGraph(attributePaths = {"category", "category.parent"})
    Page<Product> listProducts(@Nullable String nameLike, Collection<Long> categories,
        Pageable page);
}
