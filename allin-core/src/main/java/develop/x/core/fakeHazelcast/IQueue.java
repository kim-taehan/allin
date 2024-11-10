package develop.x.core.fakeHazelcast;

import java.util.concurrent.ArrayBlockingQueue;

public class IQueue extends ArrayBlockingQueue<Object> {

    public IQueue() {
        super(1000);
    }
}