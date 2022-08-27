package com.example.stock.domain;

import javax.persistence.*;

@Entity
public class Stock {
    // Id, productId, quantity

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    /*
        Optimistic Lock 을 사용하기 위한 설정
     */
    @Version
    private Long version;

    private Long quantity;

    public Stock() {
    }

    public Stock( Long productId, Long quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void decrease(Long quantity) {
        if (this.quantity - quantity < 0) {
            throw new RuntimeException("재고부족");
        }

        this.quantity -= quantity;
    }
}
