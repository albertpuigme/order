package net.apuig.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderEventRepository extends JpaRepository<OrderEvent, Long>
{
    List<OrderEvent> findByOrderId(Long orderId);
}
