package develop.x.simulator.game.service;

import develop.x.simulator.game.dto.BankRequest;
import develop.x.simulator.game.dto.QueryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BankService {

    public BankRequest findTicket(BankRequest request){

        // todo db 를 조회해야되지만 외부에는 공개되지 않으니깐 임시 작업
        return request.updateTicketInfo(100000);
    }

}
