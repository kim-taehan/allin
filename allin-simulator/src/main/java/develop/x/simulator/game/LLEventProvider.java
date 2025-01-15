package develop.x.simulator.game;

import develop.x.simulator.game.entity.SaleType;
import develop.x.simulator.game.entity.TopLlEvnt;
import develop.x.simulator.game.entity.TopProd;
import develop.x.simulator.game.repository.TopProdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LLEventProvider {

    // todo db 조회로 변경 필요

    private final TopProdRepository topProdRepository;

    public LLEvent[] get() {


        List<TopProd> topProds = topProdRepository.findByProductTypeAndSaleType("24", SaleType.SALE);


        List<TopLlEvnt> topLlEvnt = topProds.get(0).getTopLlEvnt();

        return topLlEvnt.stream().map(
                topLlEvnt1 -> new LLEvent(topLlEvnt1.getEventName(), topLlEvnt1.getEventId(), LLType.WDL))
                .toArray(LLEvent[]::new);


    }
}
