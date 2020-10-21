package ix.ginas.models;

import gov.nih.ncats.common.util.TimeUtil;
import gsrs.AuditConfig;
import gsrs.ClearAuditorRule;
import gsrs.LuceneSpringDemoApplication;
import gsrs.TimeTraveller;
import gsrs.repository.ControlledVocabularyRepository;
import gsrs.repository.PrincipalRepository;
import gsrs.springUtils.AutowireHelper;
import ix.core.models.Keyword;
import ix.ginas.models.v1.ControlledVocabulary;
import ix.ginas.models.v1.VocabularyTerm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = LuceneSpringDemoApplication.class)
@DataJpaTest
@ActiveProfiles("test")
@Import({ClearAuditorRule.class , AuditConfig.class, AutowireHelper.class})
public class ControlledVocabRepositoryTest {

    @Autowired
    private ControlledVocabularyRepository repository;

    @Autowired
    private PrincipalRepository principalRepository;

    @Autowired
    @RegisterExtension
    ClearAuditorRule clearAuditorRule;

    @RegisterExtension
    TimeTraveller timeTraveller = new TimeTraveller(LocalDate.of(1955, 11, 05));

    private Date setDate(int year, int month, int day){
        Date date = TimeUtil.toDate(year, month, day);
        TimeUtil.setCurrentTime(date);
        return date;
    }

    @Test
    public void saveCreatesId(){

        ControlledVocabulary sut = new ControlledVocabulary();
        VocabularyTerm myVocabTerm = new VocabularyTermBuilder("myVocabTerm")
                .build();
        sut.addTerms(myVocabTerm);
        assertNull(sut.id);

        Date date = new Date(timeTraveller.freezeTime());

        repository.saveAndFlush(sut);

        assertNotNull(sut.id);
        assertEquals(date.getTime(), sut.created.getTime());
        assertEquals(date.getTime(), sut.modified.getTime());
    }

    @Test
    public void updateTerms(){

        ControlledVocabulary sut = new ControlledVocabulary();
        VocabularyTerm myVocabTerm = new VocabularyTermBuilder("myVocabTerm")
                .build();
        sut.addTerms(myVocabTerm);
        assertNull(sut.id);

        Date createDate = timeTraveller.getCurrentDate();

        ControlledVocabulary sut2 = repository.saveAndFlush(sut);

        assertNotNull(sut.id);
        assertEquals(createDate.getTime(), sut.created.getTime());
        assertEquals(createDate.getTime(), sut.modified.getTime());

        sut2.addTerms(new VocabularyTermBuilder("otherTerm")
                                                .build());


        timeTraveller.jumpAhead(1, TimeUnit.DAYS);

        Date modifiedDate = timeTraveller.getCurrentDate();
        repository.saveAndFlush(sut2);

        ControlledVocabulary sut3 = repository.getOne(sut2.id);

        System.out.println(sut2.getModified());
        assertEquals(createDate.getTime(), sut3.getCreated().getTime());
        assertEquals(modifiedDate.getTime(), sut3.getModified().getTime());

    }

    @Test
    public void addFields(){

        ControlledVocabulary sut = new ControlledVocabulary();
        Keyword keyword = new Keyword("label1", "term1");
        sut.addField(keyword);
        assertNull(sut.id);

        Date createDate = timeTraveller.getCurrentDate();

        ControlledVocabulary sut2 = repository.saveAndFlush(sut);

        assertNotNull(sut.id);
        assertEquals(createDate.getTime(), sut.created.getTime());
        assertEquals(createDate.getTime(), sut.modified.getTime());

        sut2.addField(new Keyword("label2", "term2"));


        timeTraveller.jumpAhead(1, TimeUnit.DAYS);

        Date modifiedDate = timeTraveller.getCurrentDate();
        repository.saveAndFlush(sut2);

        ControlledVocabulary sut3 = repository.getOne(sut2.id);

        assertEquals(createDate.getTime(), sut3.getCreated().getTime());
        assertEquals(modifiedDate.getTime(), sut3.getModified().getTime());

        assertEquals(Arrays.asList(new Keyword("label1", "term1"),
                new Keyword("label2", "term2")),

                sut3.getFields());
    }

}
