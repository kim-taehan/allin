package develop.x.simulator.game.dto;

import develop.x.simulator.game.service.enums.AgencyInputType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShopTest {

    @Test
    @DisplayName("init() 은 기본값(agencyInputType=AUTO, agencyId=\"\", shopId=60011, tagId=1)으로 생성한다.")
    void init_setsDefaults() {
        // when
        Shop shop = Shop.init();

        // then
        assertThat(shop.agencyInputType()).isEqualTo(AgencyInputType.AUTO);
        assertThat(shop.agencyId()).isEmpty();
        assertThat(shop.shopId()).isEqualTo("60011");
        assertThat(shop.tagId()).isEqualTo("1");
    }
}
