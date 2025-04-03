package develop.x.betting.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MemoryService extends Remote {
    long getMaxHeapSize() throws RemoteException;
    long getUsedHeapSize() throws RemoteException;
}