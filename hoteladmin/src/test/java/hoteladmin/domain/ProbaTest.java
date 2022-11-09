package hoteladmin.domain;

import static org.assertj.core.api.Assertions.assertThat;

import hoteladmin.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ProbaTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Proba.class);
        Proba proba1 = new Proba();
        proba1.setId(1L);
        Proba proba2 = new Proba();
        proba2.setId(proba1.getId());
        assertThat(proba1).isEqualTo(proba2);
        proba2.setId(2L);
        assertThat(proba1).isNotEqualTo(proba2);
        proba1.setId(null);
        assertThat(proba1).isNotEqualTo(proba2);
    }
}
