package develop.x.betting.rmi;

import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

@Service
public class MemoryServiceImpl implements MemoryService {

    private final MemoryMXBean memoryMXBean;

    protected MemoryServiceImpl() throws RemoteException {
        super();
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
    }

    @Override
    public long getMaxHeapSize() throws RemoteException {
        return memoryMXBean.getHeapMemoryUsage().getMax();
    }

    @Override
    public long getUsedHeapSize() throws RemoteException {
        return memoryMXBean.getHeapMemoryUsage().getUsed();
    }
}