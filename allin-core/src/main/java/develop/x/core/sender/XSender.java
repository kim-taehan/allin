package develop.x.core.sender;

import develop.x.io.XRequest;
import develop.x.io.network.XTarget;

public interface XSender {
    boolean send(XTarget target, XRequest request);
}
