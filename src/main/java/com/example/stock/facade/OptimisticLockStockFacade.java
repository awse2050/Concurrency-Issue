package com.example.stock.facade;


import com.example.stock.service.OptimisticLockService;
import org.springframework.stereotype.Service;

@Service
public class OptimisticLockStockFacade {

    private final OptimisticLockService optimisticLockService;

    public OptimisticLockStockFacade(OptimisticLockService optimisticLockService) {
        this.optimisticLockService = optimisticLockService;
    }

    /*
        여러 쓰레드가 동시에 들어왔을 때 같은 데이터를 조회하게 되지만,
        실제 감소로직을 진행할 때 Version값을 변경시키기 때문에
        다른 쓰레드가 조금 늦게 로직을 진행하게 되면 이미 Version이 변경됐기 때문에
        없는 데이터를 가지고 쿼리를 실행한 것과 동일한 현상이 되기 떄문에, 실패하면
        이후 텀을 둔 다음 재시도를 하는 방식으로 구현한다.
     */
    public void decrease(Long id, Long quantity) throws InterruptedException {
        while (true) {
            try {
                optimisticLockService.decrease(id, quantity);
                break;
            } catch (Exception e) {
                Thread.sleep(50);
            }
        }
    }
}
