package develop.x.simulator.game.service;

import develop.x.simulator.game.dto.CancelRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CancelService {

    public CancelRequest findTicket(CancelRequest request){

        // todo db 를 조회해야되지만 외부에는 공개되지 않으니깐 임시 작업
        return request.updateTicketInfo("24", "1000", "16024", "16");
    }

}
