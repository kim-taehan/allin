package develop.x.network;

import develop.x.io.XRequest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NettyBlockingQueue {

    private final static BlockingQueue<XRequest> blockingQueue = new LinkedBlockingQueue<>();

    public void put(XRequest xRequest) throws InterruptedException {
        blockingQueue.put(xRequest);
    }

    public XRequest take() throws InterruptedException {
        return blockingQueue.take();
    }


    public void run() {

    }



}
