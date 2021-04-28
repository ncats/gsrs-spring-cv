package gsrs.cv;

import gsrs.controller.EtagLegacySearchEntityController;
import gsrs.controller.GsrsRestApiController;
import gsrs.controller.IdHelpers;
import gsrs.legacy.LegacyGsrsSearchService;
import gsrs.repository.EditRepository;
import gsrs.service.GsrsEntityService;
import ix.ginas.models.v1.ControlledVocabulary;
//import org.hibernate.search.backend.lucene.LuceneExtension;
//import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
//import org.hibernate.search.engine.search.predicate.dsl.MatchPredicateFieldStep;
//import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
//import org.hibernate.search.engine.search.query.SearchResult;
//import org.hibernate.search.mapper.orm.search.query.dsl.HibernateOrmSearchQuerySelectStep;
//import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.ExposesResourceFor;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * GSRS Rest API controller for the {@link ControlledVocabulary} entity.
 */
@ExposesResourceFor(ControlledVocabulary.class)
@GsrsRestApiController(context = ControlledVocabularyEntityService.CONTEXT,  idHelper = IdHelpers.NUMBER)
public class CvController extends EtagLegacySearchEntityController<CvController, ControlledVocabulary, Long> {
    @Autowired
    private CvLegacySearchService cvLegacySearchService;

    @Autowired
    private EditRepository editRepository;

    @Autowired
    private GsrsEntityService<ControlledVocabulary, Long> entityService;

    @Override
    protected LegacyGsrsSearchService<ControlledVocabulary> getlegacyGsrsSearchService() {
        return cvLegacySearchService;
    }

    @Override
    protected Optional<EditRepository> editRepository() {
        return Optional.of(editRepository);
    }

    @Override
    protected GsrsEntityService<ControlledVocabulary, Long> getEntityService() {
        return entityService;
    }

    @Override
    protected Stream<ControlledVocabulary> filterStream(Stream<ControlledVocabulary> stream, boolean publicOnly, Map<String, String> parameters) {
        return stream;
    }


}
