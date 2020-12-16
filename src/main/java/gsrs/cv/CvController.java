package gsrs.cv;

import gsrs.controller.EtagLegacySearchEntityController;
import gsrs.controller.GsrsRestApiController;
import gsrs.controller.IdHelpers;
import gsrs.legacy.LegacyGsrsSearchService;
import ix.ginas.models.v1.ControlledVocabulary;
//import org.hibernate.search.backend.lucene.LuceneExtension;
//import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
//import org.hibernate.search.engine.search.predicate.dsl.MatchPredicateFieldStep;
//import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
//import org.hibernate.search.engine.search.query.SearchResult;
//import org.hibernate.search.mapper.orm.search.query.dsl.HibernateOrmSearchQuerySelectStep;
//import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * GSRS Rest API controller for the {@link ControlledVocabulary} entity.
 */
@GsrsRestApiController(context = ControlledVocabularyEntityService.CONTEXT,  idHelper = IdHelpers.NUMBER)
public class CvController extends EtagLegacySearchEntityController<CvController, ControlledVocabulary, Long> {
    @Autowired
    private CvLegacySearchService cvLegacySearchService;

    @Override
    protected LegacyGsrsSearchService<ControlledVocabulary> getlegacyGsrsSearchService() {
        return cvLegacySearchService;
    }

    public CvController() {
    }


}
