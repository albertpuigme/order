package net.apuig.product;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import jakarta.annotation.Nullable;

// TODO perhaps we could skip categories without products ??
public interface CategoryRepository extends JpaRepository<Category, Long>
{
    Category findByName(String name);

    @Query("SELECT id FROM Category WHERE parent IS NULL")
    Long getRootCategoryId();

    final static String CATEGORY_HIERARCHY = """
    WITH RECURSIVE category_hierarchy(id, parent_id, parent_name, name, level) AS (
        SELECT id, parent_id, name, name, 0
        FROM categories
        WHERE id = :fromParentCategoryId
        UNION ALL
           SELECT c.id, c.parent_id, hierarchy.name, c.name, hierarchy.level + 1
           FROM categories c
           JOIN category_hierarchy hierarchy ON c.parent_id = hierarchy.id
    )
    """;

    @Query(nativeQuery = true, value = CATEGORY_HIERARCHY + """
    SELECT id, parent_id, parent_name, name, level
    FROM category_hierarchy
    WHERE level > 0
    AND (:nameLike IS NULL OR name LIKE :nameLike)
    """, countQuery = CATEGORY_HIERARCHY + """
    SELECT COUNT(id)
    FROM category_hierarchy
    WHERE level > 0
    AND (:nameLike IS NULL OR name LIKE :nameLike)
    """)
    public Page<CategoryProjection> listCategories(@Nullable String nameLike,
        Long fromParentCategoryId, Pageable page);

    @Query(nativeQuery = true, value = CATEGORY_HIERARCHY + """
    SELECT id
    FROM category_hierarchy
    WHERE level >= 0
          """)
    public Set<Long> listCategoriesIdsDownCurrent(Long fromParentCategoryId);
}
