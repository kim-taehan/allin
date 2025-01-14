package develop.x.simulator.game.service;

import develop.x.simulator.game.dto.request.LLBuyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    @Override
    public boolean buy(LLBuyRequest request) {

        cachedThreadPool.execute(() -> {
            try (Socket socket = new Socket("127.0.0.1", 45672)) {
                OutputStream outputStream = socket.getOutputStream();
                //outputStream.write(xRequest.toByte());
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });




        return false;
    }
}
