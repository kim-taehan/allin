package develop.x.betting.rmi;


import java.rmi.Naming;

class MemoryServiceImplTest {

    public static void main(String[] args) {
        try {
            MemoryService memoryService = (MemoryService) Naming.lookup("rmi://localhost:1099/MemoryService");
            System.out.println("✅ Max Heap Size: " + memoryService.getMaxHeapSize());
            System.out.println("✅ Used Heap Size: " + memoryService.getUsedHeapSize());

            System.out.println("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}