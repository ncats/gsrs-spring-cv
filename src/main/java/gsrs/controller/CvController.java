package gsrs.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gsrs.CvUtils;
import gsrs.legacy.CvLegacySearchService;
import gsrs.repository.ControlledVocabularyRepository;
import ix.core.search.SearchOptions;
import ix.core.search.SearchResult;
import ix.core.search.text.TextIndexer;
import ix.ginas.models.v1.ControlledVocabulary;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.CharStream;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
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

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GSRS Rest API controller for the {@link ControlledVocabulary} entity.
 */
@GsrsRestApiController(context ="vocabularies",  idHelper = IdHelpers.NUMBER)
public class CvController extends AbstractGsrsEntityController<ControlledVocabulary, Long> {

    private static Pattern NUMBER_PATTERN = Pattern.compile("^"+ IdHelpers.NUMBER.getRegexAsString()+"$");

    @Autowired
    private ControlledVocabularyRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

//    @Autowired
//    private CvSearchService searchService;

    @Autowired
    private CvLegacySearchService cvLegacySearchService;

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

    @Override
    protected SearchResultPair indexSearchV1(String query, Optional<Integer> top, Optional<Integer> skip, Optional<Integer> fdim,
                                                       Map<String, String[]> requestParameters) {
        SearchOptions.Builder builder = new SearchOptions.Builder()
                                                .kind(ControlledVocabulary.class);
        top.ifPresent( t-> builder.top(t));
        skip.ifPresent( t-> builder.skip(t));
        fdim.ifPresent( t-> builder.fdim(t));
        builder.withParameters(requestParameters);

//        SearchOptions searchRequest = builder.build();
//        builder.query(query);
/*
SearchRequest req = builder
                .top(top)
                .skip(skip)
                .fdim(fdim)
                .kind(kind)
                .withRequest(request()) // I don't like this,
                                        // I like being explicit,
                                        // but it's ok for now
                .query(q)
                .build();
 */
        try {
            SearchResult result = cvLegacySearchService.search(query, builder.build() );
            List<Object> results = new ArrayList<>();

            result.copyTo(results, 0, top.orElse(10), true); //this looks wrong, because we're not skipping
            //anything, but it's actually right,
            //because the original request did the skipping.
            //This mechanism should probably be worked out
            //better, as it's not consistent.
            return new SearchResultPair(result, results);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //        SearchSession session = searchService.createSearchSession();
//        List<ControlledVocabulary> dslHits = parseQueryIntoMatch(query , session).hits();
//
//
//       System.out.println("dslHits = " + dslHits);
//        return dslHits;
        return null;
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

    }

    @Override
    protected TextIndexer.TermVectors getTermVectors(Optional<String> field) throws IOException {
        return cvLegacySearchService.getTermVectors(field);
    }

    @Override
    protected TextIndexer.TermVectors searchFacetField(Optional<String> query, SearchOptions options, Optional<String> field) throws IOException {
        return cvLegacySearchService.getTermVectorsFromQuery(query.orElse(null), options, field.orElse(null));
    }

    /**
     * well known fields
     */
    public static final String FIELD_KIND = "__kind";
    public static final String FIELD_ID = "id";

    private static final String ANALYZER_FIELD = "M_FIELD";
    private static final String ANALYZER_MARKER_FIELD = "ANALYZER_MARKER";
    private static final String ANALYZER_VAL_PREFIX = "ANALYZER_";


    private static final char SORT_DESCENDING_CHAR = '$';
    private static final char SORT_ASCENDING_CHAR = '^';
    private static final int EXTRA_PADDING = 2;
    private static final String FULL_TEXT_FIELD = "text";
    private static final String SORT_PREFIX = "SORT_";
    protected static final String STOP_WORD = " THE_STOP";
    protected static final String START_WORD = "THE_START ";
    public static final String GIVEN_STOP_WORD = "$";
    public static final String GIVEN_START_WORD = "^";
    static final String ROOT = "root";


    private static final Pattern START_PATTERN = Pattern.compile(GIVEN_START_WORD,Pattern.LITERAL );
    private static final Pattern STOP_PATTERN = Pattern.compile(GIVEN_STOP_WORD,Pattern.LITERAL );

    private static final Pattern LEVO_PATTERN = Pattern.compile(Pattern.quote("(-)"));
    private static final Pattern DEXTRO_PATTERN = Pattern.compile(Pattern.quote("(+)"));
    private static final Pattern RACEMIC_PATTERN = Pattern.compile(Pattern.quote("(+/-)"));

    private static final String LEVO_WORD = "LEVOROTATION";
    private static final String RACEMIC_WORD = "RACEMICROTATION";
    private static final String DEXTRO_WORD = "DEXTROROTATION";



    static Analyzer createIndexAnalyzer() {
        Map<String, Analyzer> fields = new HashMap<String, Analyzer>();
        fields.put(FIELD_ID, new KeywordAnalyzer());
        fields.put(FIELD_KIND, new KeywordAnalyzer());
        //dkatzel 2017-08 no stop words
        return new PerFieldAnalyzerWrapper(new StandardAnalyzer(), fields);
    }
    public static class IxQueryParser extends QueryParser {

        private static final Pattern ROOT_CONTEXT_ADDER=
                Pattern.compile("(\\b(?!" + ROOT + ")[^ :]*_[^ :]*[:])");

        //TODO setting the default operation to AND
        //will make split words on white space act as an && operation not an or
        //so phrases won't have to be quoted

        protected IxQueryParser(CharStream charStream) {
            super(charStream);
//			setDefaultOperator(QueryParser.AND_OPERATOR);
        }

        public IxQueryParser(String def) {
            super(def, createIndexAnalyzer());
//			setDefaultOperator(QueryParser.AND_OPERATOR);
        }

        public IxQueryParser(String string, Analyzer indexAnalyzer) {
            super(string, indexAnalyzer);
//			setDefaultOperator(QueryParser.AND_OPERATOR);
        }

        @Override
        protected Query getRangeQuery(String field, String part1, String part2, boolean startInclusive, boolean endInclusive) throws ParseException {
            Query q= super.getRangeQuery(field, part1, part2, startInclusive, endInclusive);
            //katzelda 4/14/2018
            //this is to get range queries to work with our datetimestamps
            //without having to use the lucene DateTools

            //TODO NumericRangeQuery doesn't exist anymore should we switch to Points? maybe need 2 values in index 1 as point for fast search and 1 for retriving value?
//            if (q instanceof TermRangeQuery) {
//                TermRangeQuery trq = (TermRangeQuery) q;
//                String lower = trq.getLowerTerm().utf8ToString();
//                String higher = trq.getUpperTerm().utf8ToString();
//
//                try {
//                    double low = Double
//                            .parseDouble(lower);
//                    double high = Double
//                            .parseDouble(higher);
//                    q = NumericRangeQuery.newDoubleRange("D_" +trq.getField(),
//                            low, high, trq.includesLower(),
//                            trq.includesUpper());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }

            return q;
        }
        @Override
        public Query parse(String qtext) throws ParseException {
            if (qtext != null) {
                qtext = transformQueryForExactMatch(qtext);
            }
            // add ROOT prefix to all term queries (containing '_') where not
            // otherwise specified
            qtext = ROOT_CONTEXT_ADDER.matcher(qtext).replaceAll(ROOT + "_$1");



            //If there's an error parsing, it probably needs to have
            //quotes. Likely this happens from ":" chars

            Query q = null;
            try{
                q = super.parse(qtext);
            }catch(Exception e){
                //This is not a good way to deal with dangling quotes, but it is A
                //way to do it
                try{
                    q = super.parse("\"" + qtext + "\"");
                }catch(Exception e2){
                    if(qtext.startsWith("\"")){
                        q = super.parse(qtext + "\"");
                    }else{
                        q = super.parse("\"" + qtext);
                    }
                }
            }
            return q;

        }
    }

    private static String replaceSpecialCharsForExactMatch(String in) {

        String tmp = LEVO_PATTERN.matcher(in).replaceAll(LEVO_WORD);
        tmp = DEXTRO_PATTERN.matcher(tmp).replaceAll(DEXTRO_WORD);
        tmp = RACEMIC_PATTERN.matcher(tmp).replaceAll(RACEMIC_WORD);
        return tmp;

    }

	/*
	qtext = qtext.replace(TextIndexer.GIVEN_START_WORD, TextIndexer.START_WORD);
				qtext = qtext.replace(TextIndexer.GIVEN_STOP_WORD, TextIndexer.STOP_WORD);
	 */

    private static String transformQueryForExactMatch(String in){

        String tmp =  START_PATTERN.matcher(in).replaceAll(START_WORD);
        tmp =  STOP_PATTERN.matcher(tmp).replaceAll(STOP_WORD);
        tmp =  LEVO_PATTERN.matcher(tmp).replaceAll(LEVO_WORD);

        tmp =  DEXTRO_PATTERN.matcher(tmp).replaceAll(DEXTRO_WORD);
        tmp =  RACEMIC_PATTERN.matcher(tmp).replaceAll(RACEMIC_WORD);

        return tmp;
    }

    private String[] parseFieldsFrom(String query) {
        Pattern pattern = Pattern.compile("(\\S+):");
        Matcher matcher = pattern.matcher(query);
        List<String> fields = new ArrayList<>();
        while(matcher.find()){
            //TODO here's where we could convert the '.' separators vs the '_' separators
            fields.add(matcher.group(1));
        }
        return fields.toArray(new String[fields.size()]);
    }




    @Override
    protected JsonNode toJson(ControlledVocabulary controlledVocabulary) throws IOException {
        return objectMapper.valueToTree(controlledVocabulary);
    }

    @Override
    protected ControlledVocabulary create(ControlledVocabulary controlledVocabulary) {
        return repository.save(controlledVocabulary);
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
        Matcher matcher = NUMBER_PATTERN.matcher(someKindOfId);
        if(matcher.find()){
            //is an id - this shouldn't happen anymore since we changed the routing to ignore ID
            return get(parseIdFromString(someKindOfId));
        }
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


}
