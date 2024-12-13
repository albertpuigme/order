package net.apuig.order;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import jakarta.annotation.Nullable;

public interface OrderRepository extends JpaRepository<Order, Long>
{
    Order findByIdAndUserName(Long orderId, String userName);

    Page<Order> findByUserName(String userName, Pageable page);

    @Query("""
    SELECT id FROM Order
    WHERE status = :status
    """)
    List<Long> findIdsByStatus(OrderStatusType status);

    @Query("""
    SELECT o FROM Order o 
    WHERE (:seat IS NULL OR o.seat = :seat) 
    AND o.status IN :states
    """)
    Page<Order> findBySeatAndStatusIn(@Nullable String seat, Collection<OrderStatusType> states,
        Pageable page);
}
