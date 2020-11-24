package gsrs.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gsrs.CvUtils;
import gsrs.legacy.CvLegacySearchService;
import gsrs.legacy.LegacyGsrsSearchService;
import gsrs.repository.ControlledVocabularyRepository;
import ix.core.models.ETag;
import ix.core.search.SearchOptions;
import ix.core.search.SearchRequest;
import ix.core.search.SearchResult;
import ix.core.search.text.TextIndexer;
import ix.ginas.models.v1.ControlledVocabulary;
//import org.hibernate.search.backend.lucene.LuceneExtension;
//import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
//import org.hibernate.search.engine.search.predicate.dsl.MatchPredicateFieldStep;
//import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
//import org.hibernate.search.engine.search.query.SearchResult;
//import org.hibernate.search.mapper.orm.search.query.dsl.HibernateOrmSearchQuerySelectStep;
//import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GSRS Rest API controller for the {@link ControlledVocabulary} entity.
 */
@GsrsRestApiController(context =CvController.CONTEXT,  idHelper = IdHelpers.NUMBER)
public class CvController extends EtagLegacySearchEntityController<ControlledVocabulary, Long> {
    public static final String  CONTEXT = "vocabularies";


    public CvController() {
        super(CONTEXT,  IdHelpers.NUMBER);
    }

    @Autowired
    private ControlledVocabularyRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

//    @Autowired
//    private CvSearchService searchService;

    @Autowired
    private CvLegacySearchService cvLegacySearchService;

    @Override
    protected LegacyGsrsSearchService<ControlledVocabulary> getlegacyGsrsSearchService() {
        return cvLegacySearchService;
    }

    @Override
    protected Class<ControlledVocabulary> getEntityClass() {
        return ControlledVocabulary.class;
    }

    @Override
    protected Long parseIdFromString(String idAsString) {
        return Long.parseLong(idAsString);
    }

    @Override
    protected ControlledVocabulary fromNewJson(JsonNode json) throws IOException {
        return CvUtils.adaptSingleRecord(json, objectMapper, true);

    }

    @Override
    protected Page page(long offset, long numOfRecords, Sort sort) {

        return repository.findAll(new OffsetBasedPageRequest(offset, numOfRecords, sort));
    }

    @Override
    protected void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    protected ControlledVocabulary update(ControlledVocabulary controlledVocabulary) {
        return repository.saveAndFlush(controlledVocabulary);
    }

    @Override
    protected Long getIdFrom(ControlledVocabulary entity) {
        return entity.getId();
    }

    @Override
    protected List<ControlledVocabulary> fromNewJsonList(JsonNode list) throws IOException {
        return CvUtils.adaptList(list, objectMapper, true);
    }

    @Override
    protected ControlledVocabulary fromUpdatedJson(JsonNode json) throws IOException {
        return CvUtils.adaptSingleRecord(json, objectMapper, false);
    }

    @Override
    protected List<ControlledVocabulary> fromUpdatedJsonList(JsonNode list) throws IOException {
        return CvUtils.adaptList(list, objectMapper, false);
    }


    @Override
    protected JsonNode toJson(ControlledVocabulary controlledVocabulary) throws IOException {
        return objectMapper.valueToTree(controlledVocabulary);
    }

    @Override
    protected ControlledVocabulary create(ControlledVocabulary controlledVocabulary) {
        try {
            return repository.saveAndFlush(controlledVocabulary);
        }catch(Throwable t){
            t.printStackTrace();
            throw t;
        }
    }

    @Override
    protected long count() {
        return repository.count();
    }

    @Override
    protected Optional<ControlledVocabulary> get(Long id) {
        return repository.findById(id);
    }

    @Override
    protected Optional<ControlledVocabulary> flexLookup(String someKindOfId) {
//        Matcher matcher = NUMBER_PATTERN.matcher(someKindOfId);
//        if(matcher.find()){
//            //is an id - this shouldn't happen anymore since we changed the routing to ignore ID
//            return get(parseIdFromString(someKindOfId));
//        }
        //is the string a domain?
        List<ControlledVocabulary> list = repository.findByDomain(someKindOfId);
        if(list.isEmpty()){
            return Optional.empty();
        }
        //get first one?
        return Optional.ofNullable(list.get(0));
    }

    @Override
    protected Optional<Long> flexLookupIdOnly(String someKindOfId) {
        //easiest way to avoid deduping data is to just do a full flex lookup and then return id
        Optional<ControlledVocabulary> found = flexLookup(someKindOfId);
        if(found.isPresent()){
            return Optional.of(found.get().getId());
        }
        return Optional.empty();
    }


//    private SearchResult<ControlledVocabulary> parseQueryIntoMatch(String query, SearchSession session) {
//        Pattern pattern = Pattern.compile("(\\S+):(\\S+)");
//        Matcher matcher = pattern.matcher(query);
//
//        Map<String, List<String>> map = new LinkedHashMap<>();
//        while (matcher.find()) {
//            map.computeIfAbsent(matcher.group(1), k -> new ArrayList<>()).add(matcher.group(2));
//
//        }
//        if (map.isEmpty()) {
//            return session.search(ControlledVocabulary.class).where(f -> f.matchAll()).fetchAll();
//        }
//        if (map.size() == 1) {
//            Map.Entry<String, List<String>> entry = map.entrySet().iterator().next();
//            if (entry.getValue().size() == 1) {
//                //simpliest case
//                return session.search(ControlledVocabulary.class)
//                        .where(f -> f.match().field(entry.getKey())
//                                .matching(entry.getValue().get(0))
//
//                        ).fetchAll();
//            }
//
//            return session.search(ControlledVocabulary.class).where(f -> {
//                        BooleanPredicateClausesStep<?> step = f.bool();
//                        Iterator<String> values = entry.getValue().iterator();
//                        while (values.hasNext()) {
//                            step = step.should(f.match().field(entry.getKey())
//                                    .matching(values.next()));
//                        }
//
//                        return step;
//                    }
//            ).fetchAll();
//
//        }else{
//            //more complicated version probably need to make an AST
//            return null;
//        }
//
//
//
//    }

//    @Override
//    protected List<ControlledVocabulary> indexSearchV2(LuceneSearchRequestOp op, Optional<Integer> top, Optional<Integer> skip, Optional<Integer> fdim) {
//        SearchSession session = searchService.createSearchSession();
//
//        return session.search(ControlledVocabulary.class)
//                .where(f-> op.doIt(f))
//                .fetchHits(skip.orElse(null),top.orElse(null));
//
//    }

//    @Override
//    protected SearchResult indexSearchV1(SearchRequest searchRequest) throws Exception{
//
//            return getlegacyGsrsSearchService().search(searchRequest.getQuery(), searchRequest.getOptions() );
//

        //        SearchSession session = searchService.createSearchSession();
//        List<ControlledVocabulary> dslHits = parseQueryIntoMatch(query , session).hits();
//
//
//       System.out.println("dslHits = " + dslHits);
//        return dslHits;
//        String[] fields = parseFieldsFrom(query);
//        QueryParser parser;
//        if(fields.length==1){
//            parser = new QueryParser(fields[0], new KeywordAnalyzer());
//        }else{
//            parser = new MultiFieldQueryParser(fields, new KeywordAnalyzer());
//        }
//        System.out.println("parsed fields =" + Arrays.toString(fields));

//        QueryParser parser = new IxQueryParser(query);
//        List<ControlledVocabulary> hits = session.search( ControlledVocabulary.class )
//                .extension( LuceneExtension.get() )
//                .where( f -> {
//                    try {
//                        return f.fromLuceneQuery( parser.parse(query) );
//                    } catch (ParseException e) {
//                        return Sneak.sneakyThrow(new RuntimeException(e));
//                    }
//                })
//                .fetchHits(skip.orElse(null), top.orElse(null));

//        System.out.println("found # hits = " + hits.size());
//        return hits;

//    }




}
