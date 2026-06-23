package com.hotel.system.service;

import com.hotel.system.dto.OrderCreateRequest;
import com.hotel.system.entity.Order;
import com.hotel.system.entity.Order.OrderStatus;
import com.hotel.system.entity.OrderItem;
import com.hotel.system.entity.Product;
import com.hotel.system.entity.User;
import com.hotel.system.exception.ResourceNotFoundException;
import com.hotel.system.repository.OrderRepository;
import com.hotel.system.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final ProductRepository productRepository;

    public OrderService(
        OrderRepository orderRepository,
        ProductService productService,
        ProductRepository productRepository
    ) {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Order> getMyOrders(User currentUser) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
    }

    @Transactional(readOnly = true)
    public Order getMyOrder(User currentUser, Long orderId) {
        return orderRepository.findByIdAndUserId(orderId, currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("訂單不存在"));
    }

    @Transactional
    public Order createOrder(User currentUser, OrderCreateRequest request) {
        Product product = productService.getProduct(request.getProductId());

        if (product.getStock() < request.getQuantity()) {
            throw new IllegalStateException("庫存不足");
        }

        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = Order.builder()
            .user(currentUser)
            .totalAmount(subtotal)
            .status(OrderStatus.PAID)
            .createdAt(LocalDateTime.now())
            .build();

        OrderItem item = OrderItem.builder()
            .order(order)
            .product(product)
            .productName(product.getName())
            .unitPrice(product.getPrice())
            .quantity(request.getQuantity())
            .subtotal(subtotal)
            .build();

        order.getItems().add(item);
        product.setStock(product.getStock() - request.getQuantity());
        productRepository.save(product);

        return orderRepository.save(order);
    }

    @Transactional
    public void cancelMyOrder(User currentUser, Long orderId) {
        Order order = getMyOrder(currentUser, orderId);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("此訂單已取消");
        }
        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("此訂單狀態無法取消");
        }

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}