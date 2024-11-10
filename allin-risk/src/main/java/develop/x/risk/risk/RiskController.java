package develop.x.risk.risk;

import develop.x.core.dispatcher.annotation.XController;
import develop.x.core.dispatcher.annotation.XMapping;
import develop.x.core.dispatcher.annotation.XParam;
import develop.x.io.model.XHeaderOld;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@XController
@Slf4j
@RequiredArgsConstructor
public class RiskController {


    // 정상 호출되는 일반적인 API
    @XMapping("/normal-api")
    public void execute(@XParam("url") String url, XHeaderOld header) {
        log.info("RiskController call {}, transId={}" ,
                url, header.getTransactionId());
    }

    }