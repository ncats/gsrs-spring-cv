package gsrs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gsrs.cv.ControlledVocabularyEntityService;
import gsrs.junit.TimeTraveller;
import gsrs.service.GsrsEntityService;
import gsrs.startertests.jupiter.AbstractGsrsJpaEntityJunit5Test;
import gsrs.startertests.GsrsJpaTest;
import gsrs.startertests.jupiter.ResetAllCacheSupplierBeforeEachExtension;
import gsrs.startertests.TestGsrsValidatorFactory;
import gsrs.validator.ValidatorConfig;
import ix.core.validator.GinasProcessingMessage;
import ix.core.validator.ValidationMessage;
import ix.core.validator.ValidationResponse;
import ix.core.validator.ValidatorCallback;
import ix.ginas.models.v1.ControlledVocabulary;
import ix.ginas.utils.validation.ValidatorPlugin;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static gsrs.assertions.GsrsMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
@GsrsJpaTest(classes = {LuceneSpringDemoApplication.class, ControlledVocabularyEntityService.class})
@Import(CvServiceTest.TestConfig.class)
public class CvServiceTest extends AbstractGsrsJpaEntityJunit5Test {

    @TestConfiguration
    public static class TestConfig{
        @Bean
        public ObjectMapper objectMapper(){
            return new ObjectMapper();
        }
    }

    @RegisterExtension
    ResetAllCacheSupplierBeforeEachExtension resetAllCacheSupplierExtension = new ResetAllCacheSupplierBeforeEachExtension();

    @Autowired
    private ControlledVocabularyEntityService controlledVocabularyEntityService;
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TestGsrsValidatorFactory validatorFactory;

    @RegisterExtension
    TimeTraveller timeTraveller = new TimeTraveller(LocalDate.of(1955, 11, 05));

    @BeforeEach
    public void resetValidator(){
        validatorFactory.setValidatorsForContext(controlledVocabularyEntityService.getContext());
    }
    @Test
    public void noRecords(){
        assertEquals(0, controlledVocabularyEntityService.count());
    }

    private JsonNode toJson(Object o){
        return mapper.valueToTree(o);
    }
    @Test
    public void loadOneRecord() throws IOException {
        ControlledVocabulary vocab = new ControlledVocabulary();
        vocab.setDomain("myDomain");

        GsrsEntityService.CreationResult<ControlledVocabulary> result = controlledVocabularyEntityService.createEntity(toJson(vocab));

        assertTrue(result.isCreated());
        assertEquals(1, controlledVocabularyEntityService.count());
        ControlledVocabulary saved = result.getCreatedEntity();

        ControlledVocabulary expected = new ControlledVocabulary();
        expected.setDomain(vocab.getDomain());
        expected.setCreated(timeTraveller.getWhereWeAre().asDate());
        expected.setModified(timeTraveller.getWhereWeAre().asDate());
        assertThat(saved, allOf(
                    matchesExample(expected),
                    hasProperty("version", equalTo(1L)),
                    hasProperty("id", notNullValue())
                ));
    }

    @Test
    public void loadOneRecordWithWarningsShouldStillCreate() throws IOException {
        ControlledVocabulary vocab = new ControlledVocabulary();
        vocab.setDomain("myDomain");

        throwWarningValidation();

        GsrsEntityService.CreationResult<ControlledVocabulary> result = controlledVocabularyEntityService.createEntity(toJson(vocab));

        assertTrue(result.isCreated());
        assertEquals(1, controlledVocabularyEntityService.count());
        ControlledVocabulary saved = result.getCreatedEntity();

        ControlledVocabulary expected = new ControlledVocabulary();
        expected.setDomain(vocab.getDomain());
        expected.setCreated(timeTraveller.getWhereWeAre().asDate());
        expected.setModified(timeTraveller.getWhereWeAre().asDate());
        assertThat(saved, allOf(
                matchesExample(expected),
                hasProperty("version", equalTo(1L)),
                hasProperty("id", notNullValue())
        ));
    }

    @Test
    public void validatorNoValidationShouldPass() throws Exception {
        ControlledVocabulary vocab = new ControlledVocabulary();
        vocab.setDomain("myDomain");

        ValidationResponse<ControlledVocabulary> resp = controlledVocabularyEntityService.validateEntity(toJson(vocab));

        assertTrue(resp.isValid());
        assertTrue(resp.getValidationMessages().isEmpty());
    }

    @Test
    public void validator1WarningShouldPass() throws Exception {

        throwWarningValidation();
        ControlledVocabulary vocab = new ControlledVocabulary();
        vocab.setDomain("myDomain");

        ValidationResponse<ControlledVocabulary> resp = controlledVocabularyEntityService.validateEntity(toJson(vocab));
//        System.out.println(resp.getValidationMessages());
        assertTrue(resp.isValid());
        assertEquals(Arrays.asList(GinasProcessingMessage.WARNING_MESSAGE("a warning")), resp.getValidationMessages());
    }

    private void throwWarningValidation() {
        validatorFactory.addValidator(controlledVocabularyEntityService.getContext(), ValidatorConfig.builder()
                .newObjClass(ControlledVocabulary.class)
                .validatorClass(WarningValidator.class).build());
    }

    @Test
    public void validator1ErrorShouldFail() throws Exception {

        throwErrorValidation();
        ControlledVocabulary vocab = new ControlledVocabulary();
        vocab.setDomain("myDomain");

        ValidationResponse<ControlledVocabulary> resp = controlledVocabularyEntityService.validateEntity(toJson(vocab));
//        System.out.println(resp.getValidationMessages());
        assertFalse(resp.isValid());
        assertEquals(Arrays.asList(GinasProcessingMessage.ERROR_MESSAGE("an error")), resp.getValidationMessages());
    }

    @Test
    public void loadWithValidationErrorShouldFail() throws Exception {

        throwErrorValidation();
        ControlledVocabulary vocab = new ControlledVocabulary();
        vocab.setDomain("myDomain");

        GsrsEntityService.CreationResult<ControlledVocabulary> result = controlledVocabularyEntityService.createEntity(toJson(vocab));
        assertFalse(result.isCreated());

        ValidationResponse<ControlledVocabulary> resp = result.getValidationResponse();
        assertFalse(resp.isValid());
        assertEquals(Arrays.asList(GinasProcessingMessage.ERROR_MESSAGE("an error")), resp.getValidationMessages());


        assertEquals(0L, controlledVocabularyEntityService.count());
    }


    @Test
    public void loadWithValidationErrorAndWarningShouldFail() throws Exception {

        throwErrorValidation();
        throwWarningValidation();

        ControlledVocabulary vocab = new ControlledVocabulary();
        vocab.setDomain("myDomain");

        GsrsEntityService.CreationResult<ControlledVocabulary> result = controlledVocabularyEntityService.createEntity(toJson(vocab));
        assertFalse(result.isCreated());

        ValidationResponse<ControlledVocabulary> resp = result.getValidationResponse();
        assertFalse(resp.isValid());
        assertEquals(Arrays.asList(GinasProcessingMessage.ERROR_MESSAGE("an error"),
                GinasProcessingMessage.WARNING_MESSAGE("a warning")), resp.getValidationMessages());


        assertEquals(0L, controlledVocabularyEntityService.count());
    }

    @Test
    public void validator1Error1WarningShouldFail() throws Exception {

        throwErrorValidation();
        throwWarningValidation();

        ControlledVocabulary vocab = new ControlledVocabulary();
        vocab.setDomain("myDomain");

        ValidationResponse<ControlledVocabulary> resp = controlledVocabularyEntityService.validateEntity(toJson(vocab));
//        System.out.println(resp.getValidationMessages());
        assertFalse(resp.isValid());
        assertEquals(Arrays.asList(GinasProcessingMessage.ERROR_MESSAGE("an error"),
                GinasProcessingMessage.WARNING_MESSAGE("a warning")), resp.getValidationMessages());
    }

    private void throwErrorValidation() {
        validatorFactory.addValidator(controlledVocabularyEntityService.getContext(), ValidatorConfig.builder()
                .newObjClass(ControlledVocabulary.class)
                .validatorClass(ErrorValidator.class).build());
    }

    public static class WarningValidator implements ValidatorPlugin<Object>{

        @Override
        public boolean supports(Object newValue, Object oldValue, ValidatorConfig.METHOD_TYPE methodType) {
            return true;
        }

        @Override
        public void validate(Object objnew, Object objold, ValidatorCallback callback) {
            callback.addMessage(GinasProcessingMessage.WARNING_MESSAGE("a warning"));
        }
    }
    public static class ErrorValidator implements ValidatorPlugin<Object>{

        @Override
        public boolean supports(Object newValue, Object oldValue, ValidatorConfig.METHOD_TYPE methodType) {
            return true;
        }

        @Override
        public void validate(Object objnew, Object objold, ValidatorCallback callback) {
            callback.addMessage(GinasProcessingMessage.ERROR_MESSAGE("an error"));
        }
    }


    @Test
    public void UpdateOneRecord() throws Exception {
        ControlledVocabulary vocab = new ControlledVocabulary();
        vocab.setDomain("myDomain");

        GsrsEntityService.CreationResult<ControlledVocabulary> result = controlledVocabularyEntityService.createEntity(toJson(vocab));

        assertTrue(result.isCreated());
        assertEquals(1, controlledVocabularyEntityService.count());
        ControlledVocabulary saved = result.getCreatedEntity();

        Long id = saved.getId();
        ControlledVocabulary expected = new ControlledVocabulary();
        expected.setDomain(vocab.getDomain());
        expected.setCreated(timeTraveller.getWhereWeAre().asDate());
        expected.setModified(timeTraveller.getWhereWeAre().asDate());
        assertThat(saved, allOf(
                matchesExample(expected),
                hasProperty("version", equalTo(1L)),
                hasProperty("id", notNullValue())
        ));

        ControlledVocabulary version2 = controlledVocabularyEntityService.get(saved.getId()).get();

        version2.setDomain("domain2");

        timeTraveller.jumpAhead(1, TimeUnit.DAYS);
        GsrsEntityService.UpdateResult<ControlledVocabulary> updateResult = controlledVocabularyEntityService.updateEntity(toJson(version2));
        assertEquals(GsrsEntityService.UpdateResult.STATUS.UPDATED, updateResult.getStatus());


        ControlledVocabulary expected2 = new ControlledVocabulary();
        expected2.setDomain("domain2");
        expected2.setCreated(timeTraveller.getWhereWeWere().get().asDate());
        expected2.setModified(timeTraveller.getWhereWeAre().asDate());

        assertThat(updateResult.getUpdatedEntity(), allOf(
                matchesExample(expected2),
                hasProperty("version", equalTo(2L)),
                hasProperty("id", equalTo(id))
        ));

        assertEquals(1L, controlledVocabularyEntityService.count());

    }
}
