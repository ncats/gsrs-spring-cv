package ix.ginas.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import ix.ginas.models.v1.ControlledVocabulary;
import ix.ginas.models.v1.VocabularyTerm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JacksonTester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ControlledVocabularyTest {

    private JacksonTester<ControlledVocabulary> jacksonTester;
    @BeforeEach
    public void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        JacksonTester.initFields(this, objectMapper);
    }
    @Test
    public void roundTripJson() throws Exception{
        ControlledVocabulary sut = new ControlledVocabulary();
        VocabularyTerm myVocabTerm = new VocabularyTermBuilder("myVocabTerm")
                .build();
        sut.domain= "myDomain";
        sut.addTerms(myVocabTerm);

        String json = jacksonTester.write(sut).getJson();

        ControlledVocabulary sut2 = jacksonTester.parse(json).getObject();
        assertEquals(sut.domain, sut2.domain);
        assertTermsMatch(sut.terms, sut2.terms);
    }

    private void assertTermsMatch(List<VocabularyTerm> expected,List<VocabularyTerm> actual){
        assertEquals(expected.size(), actual.size());
        Iterator<VocabularyTerm> expectedIter = expected.iterator();
        Iterator<VocabularyTerm> actualIter = actual.iterator();
        while(expectedIter.hasNext()){
            VocabularyTerm e = expectedIter.next();
            VocabularyTerm a = actualIter.next();
            assertEquals(e.value, a.value);
            assertEquals(new ArrayList<>(e.filters), a.filters);
        }

    }

    @Test
    public void defaultTermTypeIsCVClassName(){
        ControlledVocabulary sut = new ControlledVocabulary();
        assertNull(sut.id);
        assertEquals(ControlledVocabulary.class.getName(), sut.getVocabularyTermType());
        assertFalse(sut.deprecated);
    }

    @Test
    public void changeTermType(){
        ControlledVocabulary sut = new ControlledVocabulary();
        sut.setVocabularyTermType("myType");
        assertEquals("myType", sut.getVocabularyTermType());
    }

    @Test
    public void addTerm(){
        ControlledVocabulary sut = new ControlledVocabulary();
        VocabularyTerm myVocabTerm = new VocabularyTermBuilder("myVocabTerm")
                .build();
        sut.addTerms(myVocabTerm);

        assertEquals(Arrays.asList(myVocabTerm), sut.terms   );
    }
}
