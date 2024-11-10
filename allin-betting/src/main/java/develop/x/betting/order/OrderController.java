package develop.x.betting.order;

import develop.x.core.dispatcher.XRequest;
import develop.x.core.dispatcher.annotation.XController;
import develop.x.core.dispatcher.annotation.XMapping;
import develop.x.core.dispatcher.annotation.XModel;
import develop.x.core.dispatcher.annotation.XParam;
import develop.x.core.message.XMessageFinder;
import develop.x.core.sender.XSender;
import develop.x.io.model.XHeaderOld;
import develop.x.io.network.XTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@XController
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final XMessageFinder messageFinder;

    private final XSender xSender;

    // 정상 호출되는 일반적인 API @XModel FakeXData data,
    @XMapping("/normal-api")
    public void execute(@XParam("url") String url, @XModel TestDto request) {
        log.info("1번째 API 호출됨");
        XRequest xRequest = new XRequest.Builder()
                .body(request)
                .header("url", "/next-api")
                .header("transactionId", UUID.randomUUID().toString())
                .build();
        xSender.send(XTarget.ORDER, xRequest);
    }


    @XMapping("/next-api")
    public void execute2(@XParam("url") String url, @XModel TestDto request) {
        log.info("2번째 API 호출됨");
    }

    // business model 도중 애러가 발생
//    @XMapping("/ex-api")
//    public void executeEx(@XModel FakeXData data) {
//        throw new IllegalArgumentException("강제로 애러를 발생함");
//    }
//
//
//    @XBlockingQueueMapping(OrderBlockingQueue.class)
//    public void executeEx2(@XModel FakeXData data) {
//        log.info("bq call {}" , data);
//    }
}
