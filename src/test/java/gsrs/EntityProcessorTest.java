package gsrs;

import gsrs.entityProcessor.ConfigBasedEntityProcessorConfiguration;
import gsrs.repository.ControlledVocabularyRepository;
import gsrs.repository.PrincipalRepository;
import gsrs.springUtils.AutowireHelper;
import gsrs.startertests.GsrsJpaTest;
import gsrs.startertests.TestEntityProcessorFactory;
import gsrs.startertests.jupiter.AbstractGsrsJpaEntityJunit5Test;
import gsrs.startertests.jupiter.ResetAllEntityProcessorBeforeEachExtension;
import ix.core.EntityProcessor;
import ix.ginas.models.ControlledVocabRepositoryTest;
import ix.ginas.models.v1.ControlledVocabulary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = {LuceneSpringDemoApplication.class})
@GsrsJpaTest
@ActiveProfiles("test")
//@Import({GsrsSecurityConfig.class})
public class EntityProcessorTest extends AbstractGsrsJpaEntityJunit5Test {


    private static List<String> list = new ArrayList<>();

    @BeforeEach
    public void clearList(){
        testEntityProcessorFactory.setEntityProcessors(new MyEntityProcessor());
        list.clear();
    }


    @Autowired
    private ControlledVocabularyRepository repository;

    @Autowired
    private PrincipalRepository principalRepository;

    @Autowired
    private TestEntityProcessorFactory testEntityProcessorFactory;

    @Test
    public void prePersist(){
        ControlledVocabulary sut = new ControlledVocabulary();

        sut.setDomain("myDomain");

        repository.saveAndFlush(sut);

        assertEquals(Arrays.asList("myDomain"), list);

    }

    public static class MyEntityProcessor implements EntityProcessor<ControlledVocabulary> {

        @Override
        public void prePersist(ControlledVocabulary obj) {
            list.add(obj.getDomain());
        }

        @Override
        public Class<ControlledVocabulary> getEntityClass() {
            return ControlledVocabulary.class;
        }
    }
}
