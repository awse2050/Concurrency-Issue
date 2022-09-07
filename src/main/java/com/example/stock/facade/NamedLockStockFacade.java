package com.example.stock.facade;

import com.example.stock.repository.LockRepository;
import com.example.stock.service.StockService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NamedLockStockFacade {

    private final LockRepository lockRepository;

    private final StockService stockService;

    public NamedLockStockFacade(LockRepository lockRepository, StockService stockService) {
        this.lockRepository = lockRepository;
        this.stockService = stockService;
    }

    /*
        (부모) 해당 메서드의 decrease메서드는 전파전략이 다르게 설정되어있다.
        같은 key로 Lock을 먼저 잡고 실제 로직을 진행시킨다.
        Named Lock은 트랜잭션 종료 후에 Lock을 풀지 않는다.
        Lock을 잡은 상태에서 예외가 발생하거나 서버가 다운됐을 떄, 재고감소 로직에 상관없이 Lock을 풀어줘야 하기 때문에
        어떤 상황이 나왔던 간에 finally 를 통해서 unlock을 해줘야 한다.

        만약 같은 트랜잭션일 경우에 실제 로직에서 예외가 발생할 경우
     */
    @Transactional
    public void decrease(Long id, Long quantity) {
        try {
            lockRepository.getLock(id.toString());
            stockService.decrease(id, quantity);
        } finally {
            lockRepository.releaseLock(id.toString());
        }
    }
}
