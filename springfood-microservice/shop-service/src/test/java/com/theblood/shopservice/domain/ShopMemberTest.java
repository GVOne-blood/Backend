package com.theblood.shopservice.domain;

import static com.theblood.shopservice.domain.ShopMemberTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.theblood.shopservice.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ShopMemberTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ShopMember.class);
        ShopMember shopMember1 = getShopMemberSample1();
        ShopMember shopMember2 = new ShopMember();
        assertThat(shopMember1).isNotEqualTo(shopMember2);

        shopMember2.setShopMemberId(shopMember1.getShopMemberId());
        assertThat(shopMember1).isEqualTo(shopMember2);

        shopMember2 = getShopMemberSample2();
        assertThat(shopMember1).isNotEqualTo(shopMember2);
    }
}
