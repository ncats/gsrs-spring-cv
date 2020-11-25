package gsrs;

import gsrs.startertests.GsrsEntityTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {LuceneSpringDemoApplication.class, GsrsEntityTestConfiguration.class})
@SpringBootTest
@ActiveProfiles("test")
class LuceneSpringDemoApplicationTests {

    @Test
    void contextLoads() {
    }

}
