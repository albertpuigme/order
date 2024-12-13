package net.apuig.order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductInOrderRepository extends JpaRepository<ProductInOrder, Long>
{
    @EntityGraph(attributePaths = "product")
    List<ProductInOrder> findByOrderId(Long orderId);

    @EntityGraph(attributePaths = "product")
    List<ProductInOrder> findByOrderIdAndOrderUserName(Long orderId, String userName);

    @EntityGraph(attributePaths = "product")
    Optional<ProductInOrder> findByOrderIdAndOrderUserNameAndProductId(Long orderId,
        String userName, Long productId);

    boolean existsByOrderIdAndProductId(Long orderId, Long productId);
}
