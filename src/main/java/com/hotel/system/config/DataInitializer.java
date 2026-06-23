package com.hotel.system.config;

import com.hotel.system.entity.Product;
import com.hotel.system.entity.Room;
import com.hotel.system.repository.ProductRepository;
import com.hotel.system.repository.RoomRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoomRepository roomRepository;
    private final ProductRepository productRepository;

    public DataInitializer(RoomRepository roomRepository, ProductRepository productRepository) {
        this.roomRepository = roomRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (roomRepository.count() == 0) {
            roomRepository.save(Room.builder()
                .name("經典雙人房")
                .description("25 坪舒適空間，城市景觀，含雙人早餐。")
                .pricePerNight(new BigDecimal("3200"))
                .capacity(2)
                .build());
            roomRepository.save(Room.builder()
                .name("豪華海景套房")
                .description("45 坪海景套房，獨立客廳與浴缸，尊享禮遇。")
                .pricePerNight(new BigDecimal("6800"))
                .capacity(2)
                .build());
            roomRepository.save(Room.builder()
                .name("家庭四人房")
                .description("35 坪家庭房型，兩大床，適合親子出遊。")
                .pricePerNight(new BigDecimal("4800"))
                .capacity(4)
                .build());
        }

        if (productRepository.count() == 0) {
            productRepository.save(Product.builder()
                .name("招牌下午茶組")
                .description("飯店大廳享用，含甜點塔與飲品。")
                .price(new BigDecimal("880"))
                .stock(50)
                .build());
            productRepository.save(Product.builder()
                .name("SPA 芳療 60 分鐘")
                .description("專業芳療師服務，舒壓放鬆體驗。")
                .price(new BigDecimal("2200"))
                .stock(20)
                .build());
            productRepository.save(Product.builder()
                .name("客房迷你吧補充包")
                .description("含飲品、零食與礦泉水。")
                .price(new BigDecimal("450"))
                .stock(100)
                .build());
            productRepository.save(Product.builder()
                .name("機場接送單程")
                .description("專車接送至桃園或松山機場。")
                .price(new BigDecimal("1500"))
                .stock(30)
                .build());
        }
    }
}