package gsrs;

import gsrs.repository.ControlledVocabularyRepository;
import gsrs.security.GsrsSecurityConfig;
import gsrs.springUtils.AutowireHelper;
import ix.ginas.models.v1.ControlledVocabulary;
import ix.ginas.models.v1.VocabularyTerm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = LuceneSpringDemoApplication.class)
//this dirties context makes us recreate the h2 database afte each method (and any other context related thing)
//this not only wipes out the loaded data but resets all auto increment counters.
//without this even if we remove all entities from the repository after each test, the ids wouldn't reset back to 1
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import({ClearAuditorRule.class , AuditConfig.class, AutowireHelper.class, GsrsSecurityConfig.class})
@Transactional
public class CvControllerTest {

    @Autowired
    @RegisterExtension
    ClearAuditorRule clearAuditorRule;

    @RegisterExtension
    TimeTraveller timeTraveller = new TimeTraveller(LocalDate.of(1955, 11, 05));



    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ControlledVocabularyRepository repo;

    @Test
    public void noDataLoadedShouldHave0Results() throws Exception {
        mockMvc.perform(get("/api/v1/vocabularies"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.total", is(0)));
    }

    @Test
    public void loadSingleRecordNoTerms() throws Exception {
        ControlledVocabulary vocab = new ControlledVocabulary();
        vocab.setDomain("myDomain");
        ControlledVocabulary savedVocab = repo.saveAndFlush(vocab);

        mockMvc.perform(get("/api/v1/vocabularies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.count", is(1)))
                .andExpect(jsonPath("$.content.length()", is(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].version", is(1)))
                .andExpect(jsonPath("$.content[0].domain", is(vocab.getDomain())))
                .andExpect(jsonPath("$.content[0].terms.length()", is(0)))
                .andExpect(jsonPath("$.content[0].modified", is(timeTraveller.getCurrentTimeMillis())))
                .andExpect(jsonPath("$.content[0].created", is(timeTraveller.getCurrentTimeMillis())))
        ;


        mockMvc.perform(get("/api/v1/vocabularies/"+savedVocab.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedVocab.getId().intValue())))
                .andExpect(jsonPath("$.version", is(1)))
                .andExpect(jsonPath("$.domain", is(vocab.getDomain())))
                .andExpect(jsonPath("$.terms.length()", is(0)))
                .andExpect(jsonPath("$.created", is(timeTraveller.getCurrentTimeMillis())))
                .andExpect(jsonPath("$.modified", is(timeTraveller.getCurrentTimeMillis())))
        ;

        mockMvc.perform(get("/api/v1/vocabularies("+savedVocab.getId() + ")"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedVocab.getId().intValue())))
                .andExpect(jsonPath("$.version", is(1)))
                .andExpect(jsonPath("$.domain", is(vocab.getDomain())))
                .andExpect(jsonPath("$.terms.length()", is(0)))
        ;
    }

    @Test
    public void updateSingleRecordNoTerms() throws Exception {
        ControlledVocabulary vocab = new ControlledVocabulary();
        vocab.setDomain("myDomain");
        ControlledVocabulary savedVocab = repo.saveAndFlush(vocab);

        mockMvc.perform(get("/api/v1/vocabularies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.count", is(1)))
                .andExpect(jsonPath("$.content.length()", is(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].version", is(1)))
                .andExpect(jsonPath("$.content[0].domain", is(vocab.getDomain())))
                .andExpect(jsonPath("$.content[0].terms.length()", is(0)))
                .andExpect(jsonPath("$.content[0].created", is(timeTraveller.getCurrentTimeMillis())))
                .andExpect(jsonPath("$.content[0].modified", is(timeTraveller.getCurrentTimeMillis())))
        ;


        mockMvc.perform(get("/api/v1/vocabularies/"+savedVocab.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedVocab.getId().intValue())))
                .andExpect(jsonPath("$.version", is(1)))
                .andExpect(jsonPath("$.domain", is(vocab.getDomain())))
                .andExpect(jsonPath("$.terms.length()", is(0)))
                .andExpect(jsonPath("$.created", is(timeTraveller.getCurrentTimeMillis())))
                .andExpect(jsonPath("$.modified", is(timeTraveller.getCurrentTimeMillis())))
        ;

        mockMvc.perform(get("/api/v1/vocabularies("+savedVocab.getId() + ")"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedVocab.getId().intValue())))
                .andExpect(jsonPath("$.version", is(1)))
                .andExpect(jsonPath("$.deprecated", is(false)))
                .andExpect(jsonPath("$.domain", is(vocab.getDomain())))
                .andExpect(jsonPath("$.terms.length()", is(0)))
                .andExpect(jsonPath("$.created", is(timeTraveller.getCurrentTimeMillis())))
                .andExpect(jsonPath("$.modified", is(timeTraveller.getCurrentTimeMillis())))
        ;

        savedVocab.setDeprecated(true);

        timeTraveller.jumpAhead(1, TimeUnit.DAYS);

        repo.saveAndFlush(savedVocab);

        mockMvc.perform(get("/api/v1/vocabularies("+savedVocab.getId() + ")"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedVocab.getId().intValue())))
                .andExpect(jsonPath("$.version", is(2)))
                .andExpect(jsonPath("$.deprecated", is(true)))
                .andExpect(jsonPath("$.domain", is(vocab.getDomain())))
                .andExpect(jsonPath("$.terms.length()", is(0)))
                .andExpect(jsonPath("$.created", is(timeTraveller.getWhereWeWereDate().get().getTime())))
                .andExpect(jsonPath("$.modified", is(timeTraveller.getCurrentTimeMillis())))
        ;

    }

    @Test
    public void loadSingleRecordWithTerms() throws Exception {
        ControlledVocabulary vocab = new ControlledVocabulary();
        vocab.setDomain("myDomain");
        VocabularyTerm term1 = new VocabularyTerm();
        term1.setValue("term1");
        term1.setDisplay("term1Display");
        vocab.addTerms( term1);

        VocabularyTerm term2 = new VocabularyTerm();
        term2.setValue("term2");
        term2.setDisplay("term2Display");
        vocab.addTerms( term2);

        ControlledVocabulary savedVocab = repo.saveAndFlush(vocab);

        mockMvc.perform(get("/api/v1/vocabularies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.count", is(1)))
                .andExpect(jsonPath("$.content.length()", is(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].version", is(1)))
                .andExpect(jsonPath("$.content[0].domain", is(vocab.getDomain())))
                .andExpect(jsonPath("$.content[0].terms.length()", is(2)))
                .andExpect(jsonPath("$.content[0].terms[*].value", is(Arrays.asList("term1", "term2"))))
        ;


        mockMvc.perform(get("/api/v1/vocabularies/"+savedVocab.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedVocab.getId().intValue())))
                .andExpect(jsonPath("$.version", is(1)))
                .andExpect(jsonPath("$.domain", is(vocab.getDomain())))
                .andExpect(jsonPath("$.terms.length()", is(2)))
        ;

        mockMvc.perform(get("/api/v1/vocabularies("+savedVocab.getId() + ")"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedVocab.getId().intValue())))
                .andExpect(jsonPath("$.version", is(1)))
                .andExpect(jsonPath("$.domain", is(vocab.getDomain())))
                .andExpect(jsonPath("$.terms.length()", is(2)))
        ;
    }

    @Test
    public void updateSingleRecordWithNewTerms() throws Exception {
        ControlledVocabulary vocab = new ControlledVocabulary();
        vocab.setDomain("myDomain");
        VocabularyTerm term1 = new VocabularyTerm();
        term1.setValue("term1");
        term1.setDisplay("term1Display");
        vocab.addTerms( term1);

        VocabularyTerm term2 = new VocabularyTerm();
        term2.setValue("term2");
        term2.setDisplay("term2Display");
        vocab.addTerms( term2);

        repo.saveAndFlush(vocab);

        ControlledVocabulary savedVocab = repo.getOne(vocab.getId());
        System.out.println("initial save version = " + savedVocab.getVersion());
        mockMvc.perform(get("/api/v1/vocabularies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(1)))
                .andExpect(jsonPath("$.count", is(1)))
                .andExpect(jsonPath("$.content.length()", is(1)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].version", is(1)))
                .andExpect(jsonPath("$.content[0].domain", is(vocab.getDomain())))
                .andExpect(jsonPath("$.content[0].terms.length()", is(2)))
                .andExpect(jsonPath("$.content[0].terms[*].value", is(Arrays.asList("term1", "term2"))))
        ;


        mockMvc.perform(get("/api/v1/vocabularies/"+savedVocab.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedVocab.getId().intValue())))
                .andExpect(jsonPath("$.version", is(1)))
                .andExpect(jsonPath("$.domain", is(vocab.getDomain())))
                .andExpect(jsonPath("$.terms.length()", is(2)))
        ;

        mockMvc.perform(get("/api/v1/vocabularies("+savedVocab.getId() + ")"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedVocab.getId().intValue())))
                .andExpect(jsonPath("$.version", is(1)))
                .andExpect(jsonPath("$.domain", is(vocab.getDomain())))
                .andExpect(jsonPath("$.terms.length()", is(2)))
                .andExpect(jsonPath("$.created", is(timeTraveller.getCurrentTimeMillis())))
                .andExpect(jsonPath("$.modified", is(timeTraveller.getCurrentTimeMillis())))
                //TODO the json path with wildcards always returns lists so even if there are multiple objects with same value we need a list...
                .andExpect(jsonPath("$.terms[*].created", is(Arrays.asList(timeTraveller.getCurrentTimeMillis(),timeTraveller.getCurrentTimeMillis()))))
                .andExpect(jsonPath("$.terms[*].modified", is(Arrays.asList(timeTraveller.getCurrentTimeMillis(),timeTraveller.getCurrentTimeMillis()))))
        ;

        timeTraveller.jumpAhead(1, ChronoUnit.YEARS);
        VocabularyTerm term3 = new VocabularyTerm();
        term3.setValue("term3");
        term3.setDisplay("term3Display");
        savedVocab.addTerms( term3);
        System.out.println("before saveAndFlush  savedVocab version = " + savedVocab.getVersion());
        repo.saveAndFlush(savedVocab);

        ControlledVocabulary version2 = repo.getOne(savedVocab.getId());
        System.out.println("resaved vocab version = " + savedVocab.getVersion());

        System.out.println("version = " + version2.getVersion());

        mockMvc.perform(get("/api/v1/vocabularies("+savedVocab.getId() + ")"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedVocab.getId().intValue())))
                .andExpect(jsonPath("$.version", is(2)))
                .andExpect(jsonPath("$.domain", is(vocab.getDomain())))
                .andExpect(jsonPath("$.terms.length()", is(3)))
                .andExpect(jsonPath("$.created", is(timeTraveller.getWhereWeWereDate().get().getTime())))
                .andExpect(jsonPath("$.modified", is(timeTraveller.getCurrentTimeMillis())))
                .andExpect(jsonPath("$.terms[:2].created", is(Arrays.asList(timeTraveller.getWhereWeWereDate().get().getTime(), timeTraveller.getWhereWeWereDate().get().getTime()))))
                .andExpect(jsonPath("$.terms[:2].modified", is(Arrays.asList(timeTraveller.getWhereWeWereDate().get().getTime(), timeTraveller.getWhereWeWereDate().get().getTime()))))

                .andExpect(jsonPath("$.terms[2].created", is(timeTraveller.getCurrentTimeMillis())))
                .andExpect(jsonPath("$.terms[2].modified", is(timeTraveller.getCurrentTimeMillis())))
        ;
    }
}
