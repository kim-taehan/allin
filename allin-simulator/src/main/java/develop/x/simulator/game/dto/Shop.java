package develop.x.simulator.game.dto;

import develop.x.simulator.game.service.enums.AgencyInputType;
import lombok.Builder;
import org.apache.logging.log4j.util.Strings;

@Builder
public record Shop(
        AgencyInputType agencyInputType,
        String agencyId,
        String shopId,
        String tagId
) {
    public static Shop init() {
        return Shop.builder()
                .agencyInputType(AgencyInputType.AUTO)
                .agencyId(Strings.EMPTY)
                .shopId("60011")
                .tagId("1")
                .build();
    }


}
