package gsrs;

import gsrs.startertests.GsrsEntityTestConfiguration;
import gsrs.startertests.jupiter.AbstractGsrsJpaEntityJunit5Test;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {LuceneSpringDemoApplication.class, GsrsEntityTestConfiguration.class})
@SpringBootTest
@ActiveProfiles("test")
class LuceneSpringDemoApplicationTests extends AbstractGsrsJpaEntityJunit5Test {

    @Test
    void contextLoads() {
    }

}
