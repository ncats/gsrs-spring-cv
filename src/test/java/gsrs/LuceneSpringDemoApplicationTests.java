package gsrs;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = LuceneSpringDemoApplication.class)
@SpringBootTest
@ActiveProfiles("test")
class LuceneSpringDemoApplicationTests {

    @Test
    void contextLoads() {
    }

}
