package com.example.stock.facade;

import com.example.stock.repository.RedisLockRepository;
import com.example.stock.service.StockService;
import org.springframework.stereotype.Component;

@Component
public class LettuceLockStockFacade {

    private final RedisLockRepository repository;
    private final StockService stockService;

    public LettuceLockStockFacade(RedisLockRepository repository, StockService stockService) {
        this.repository = repository;
        this.stockService = stockService;
    }

    public void decrease(Long key, Long quantity) throws InterruptedException {
        // spin lock 방식이기 때문에 facade 패턴이 필요하고,
        // 실패 할 경우 계속해서 재시도를 하기때문에 부하가 생기므로 시간차이를 둔다.
        while(!repository.lock(key)) {
            Thread.sleep(100);
        }

        try{
            stockService.decrease(key, quantity);
        } finally {
            repository.unlock(key);
        }
    }
}
