package com.example.stock.facade;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class NamedLockStockFacadeTest {

    @Autowired
    private NamedLockStockFacade lockStockFacade;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    void before() {
        Stock stock = new Stock(1L, 100L);

        stockRepository.saveAndFlush(stock);
    }

    @AfterEach
    void after() {
        stockRepository.deleteAll();
    }

    /*
        현재 만든 로직의 문제점은??
        요청이 여러개 들어오게 된다면 어떻게 될 것인가.
     */
    @Test
    void stock_decrease() throws InterruptedException {
        lockStockFacade.decrease(1L, 1L);

        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertEquals(99, stock.getQuantity());
    }

    /*
      여러개의 요청을 한번에 보내줘야 하기 때문에 멀티 쓰레드를 이용해야 한다.
      ExecuteService - 비동기로 실행하는 작업을 단순화하여 할수있게 도와주는 java api

      100개의 요청이 기다려야 하므로 CountdownLatch 를 사용
      CountdownLatch -> 다른 쓰레드에서 수행중인 작업이 완료될때까지 대기할 수 있도록 도와주는 클래스이다.
   */
    @Test
    void 동시에_100개_요청() throws InterruptedException {
        // 쓰레드 개수
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i< threadCount; i++) {
            executorService.submit(() -> {
                // 한 쓰레드 당 한 개의 재고를 떨어뜨린다.
                try {
                    lockStockFacade.decrease(1L ,1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        //모든 요청 완료시 값을 조회 후 비교
        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertEquals(0L, stock.getQuantity());
    }

}