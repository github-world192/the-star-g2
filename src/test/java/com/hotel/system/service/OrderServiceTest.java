package com.hotel.system.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hotel.system.dto.OrderCreateRequest;
import com.hotel.system.entity.Order.OrderStatus;
import com.hotel.system.entity.Product;
import com.hotel.system.entity.User;
import com.hotel.system.entity.User.AuthProvider;
import com.hotel.system.entity.User.UserRole;
import com.hotel.system.exception.ResourceNotFoundException;
import com.hotel.system.repository.ProductRepository;
import com.hotel.system.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private User userA;
    private User userB;
    private Product product;

    @BeforeEach
    void setUp() {
        userA = userRepository.save(User.builder()
            .email("order-a@test.com")
            .name("Order A")
            .provider(AuthProvider.GOOGLE)
            .providerId("order-sub-a")
            .role(UserRole.GUEST)
            .createdAt(LocalDateTime.now())
            .build());

        userB = userRepository.save(User.builder()
            .email("order-b@test.com")
            .name("Order B")
            .provider(AuthProvider.GOOGLE)
            .providerId("order-sub-b")
            .role(UserRole.GUEST)
            .createdAt(LocalDateTime.now())
            .build());

        product = productRepository.save(Product.builder()
            .name("測試商品")
            .description("測試用")
            .price(new BigDecimal("500"))
            .stock(10)
            .build());
    }

    @Test
    void createAndGetMyOrder() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setProductId(product.getId());
        request.setQuantity(2);

        var order = orderService.createOrder(userA, request);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("1000");
        assertThat(orderService.getMyOrder(userA, order.getId()).getItems()).hasSize(1);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStock()).isEqualTo(8);
    }

    @Test
    void cannotAccessOtherUsersOrder() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setProductId(product.getId());
        request.setQuantity(1);

        var order = orderService.createOrder(userA, request);

        assertThatThrownBy(() -> orderService.getMyOrder(userB, order.getId()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void cancelMyOrderRestoresStock() {
        OrderCreateRequest request = new OrderCreateRequest();
        request.setProductId(product.getId());
        request.setQuantity(3);

        var order = orderService.createOrder(userA, request);
        orderService.cancelMyOrder(userA, order.getId());

        assertThat(orderService.getMyOrder(userA, order.getId()).getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStock()).isEqualTo(10);
    }
}