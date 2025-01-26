package develop.x.simulator.game.service;

import develop.x.simulator.game.dto.QueryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class QueryService {

    public QueryRequest findTicket(QueryRequest request){

        // todo db 를 조회해야되지만 외부에는 공개되지 않으니깐 임시 작업
        return request.updateTicketInfo("24", "2025", "11", "100", "101");
    }

}
