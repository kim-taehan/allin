package develop.x.betting.order;

import develop.x.core.dispatcher.annotation.XController;
import develop.x.core.dispatcher.annotation.XMapping;
import develop.x.core.dispatcher.annotation.XModel;
import develop.x.core.dispatcher.annotation.XParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@XController
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    @XMapping("/order-ll")
    // 정상 호출되는 일반적인 API @XModel FakeXData data,
    public void execute(@XParam("url") String url, @XModel OrderLLRequest request) {
        log.info("url = {}", url);
        log.info("gameId = {}", request.getGameId());
    }
}
