package develop.x.core.dispatcher;

import develop.x.core.dispatcher.argumentresolver.XArgumentProvider;
import develop.x.core.dispatcher.handler.XHandler;
import develop.x.core.dispatcher.handler.XHandlerManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract non-sealed class AbstractXDispatcher implements XDispatcher {

    private final XHandlerManager handlerManager;
    private final XArgumentProvider XArgumentProvider;

    public AbstractXDispatcher(XHandlerManager handlerManager, XArgumentProvider XArgumentProvider) {
        this.handlerManager = handlerManager;
        this.XArgumentProvider = XArgumentProvider;
    }

    @Override
    public void invoke(Object item) {

        // 1. 전달 받은 데이터를 allin 시스템에서 사용할 수 있는 XData 로 변환한다.
        //    여기서 전달받은 데이터는 여러가지 형태가 존재할 수 있다.
        //    Serializable or JsonByte, JsonString
        //    전달받을 데이터의 형태가 추가되어도 기존 코드의 변경을 없게 하기 위해 convert 를 추상화 한다.
        XRequest request = convert((byte[]) item);

        //System.out.println("xRequest = " + xRequest);

        // 2. business model 을 찾을 수 있어야 한다.
        //    http 의 경우 url 로 맵핑 하지만 여기서는 apiName = url 이라 가정하고 받을 수 있게 한다.
        XHandler handler = handlerManager.findHandler(request.getHeaders().get("url"));

        // 3. argumentResolver 기능을 통해 business model 에서 사용하려고 하는 메서드 인자값을 변환하는 기능을 추가한다.
        //    마찬가지로 하위 단계에서도 설정 가능하게 한다.
        Object[] arguments = XArgumentProvider.convertArguments(handler, request);

        // 4. business model 과 연동 이 역시 다양한 방법으로 사용할 수 있게 template method 패턴으로 던질 수 있어야 한다.
        try {
            doRun(handler, arguments);
        } catch (RuntimeException e) {
            log.error("처리할 수 없는 exception 발생하였습니다. ", e);
        }
    }

    protected abstract void doRun(XHandler handler, Object[] arguments);

    private XRequest convert(byte[] bytes) {


        return new XRequest(bytes);
    }
}
