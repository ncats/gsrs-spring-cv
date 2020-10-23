package gsrs.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.nih.ncats.common.sneak.Sneak;
import gsrs.indexer.CvSearchService;
import gsrs.repository.ControlledVocabularyRepository;
import ix.ginas.models.v1.CodeSystemControlledVocabulary;
import ix.ginas.models.v1.ControlledVocabulary;
import ix.ginas.models.v1.FragmentControlledVocabulary;
import ix.ginas.models.v1.VocabularyTerm;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.CharStream;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.hibernate.search.backend.lucene.LuceneExtension;
import org.hibernate.search.engine.search.predicate.dsl.BooleanPredicateClausesStep;
import org.hibernate.search.engine.search.predicate.dsl.MatchPredicateFieldStep;
import org.hibernate.search.engine.search.predicate.dsl.SearchPredicateFactory;
import org.hibernate.search.engine.search.query.SearchResult;
import org.hibernate.search.mapper.orm.search.query.dsl.HibernateOrmSearchQuerySelectStep;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@GsrsRestApiController(context ="vocabularies",  idHelper = IdHelpers.NUMBER)
public class CvController extends GsrsEntityController<ControlledVocabulary, Long> {

    private static Pattern NUMBER_PATTERN = Pattern.compile("^"+ IdHelpers.NUMBER.getRegexAsString()+"$");
    //these fields are used to figure out which ControlledVocabulary subclass to use
    private static Set<String> fragmentDomains;
    private static Set<String> codeSystemDomains;
    static {
        fragmentDomains = new HashSet<>();
        fragmentDomains.add("NUCLEIC_ACID_SUGAR");
        fragmentDomains.add("NUCLEIC_ACID_LINKAGE");
        fragmentDomains.add("NUCLEIC_ACID_BASE");
        fragmentDomains.add("AMINO_ACID_RESIDUE");

        codeSystemDomains = new HashSet<>();
        codeSystemDomains.add("CODE_SYSTEM");
        codeSystemDomains.add("DOCUMENT_TYPE");
    }

    @Autowired
    private ControlledVocabularyRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CvSearchService searchService;

    @Override
    protected Long parseIdFromString(String idAsString) {
        return Long.parseLong(idAsString);
    }

    @Override
    protected ControlledVocabulary fromJson(JsonNode json) throws IOException {
        return adaptSingleRecord(json);

    }

    @Override
    protected List<ControlledVocabulary> fromJsonList(JsonNode list) throws IOException {
        List<ControlledVocabulary> adaptedCvs = new ArrayList<>(list.size());
        for(JsonNode cvValue: list){

            ControlledVocabulary cv = adaptSingleRecord(cvValue);
            //the Play version called cv.save() here we won't
            adaptedCvs.add(cv);


        }
        return adaptedCvs;
    }

    private SearchResult<ControlledVocabulary> parseQueryIntoMatch(String query, SearchSession session) {
        Pattern pattern = Pattern.compile("(\\S+):(\\S+)");
        Matcher matcher = pattern.matcher(query);

        Map<String, List<String>> map = new LinkedHashMap<>();
        while (matcher.find()) {
            map.computeIfAbsent(matcher.group(1), k -> new ArrayList<>()).add(matcher.group(2));

        }
        if (map.isEmpty()) {
            return session.search(ControlledVocabulary.class).where(f -> f.matchAll()).fetchAll();
        }
        if (map.size() == 1) {
            Map.Entry<String, List<String>> entry = map.entrySet().iterator().next();
            if (entry.getValue().size() == 1) {
                //simpliest case
                return session.search(ControlledVocabulary.class)
                        .where(f -> f.match().field(entry.getKey())
                                .matching(entry.getValue().get(0))

                        ).fetchAll();
            }

            return session.search(ControlledVocabulary.class).where(f -> {
                        BooleanPredicateClausesStep<?> step = f.bool();
                        Iterator<String> values = entry.getValue().iterator();
                        while (values.hasNext()) {
                            step = step.should(f.match().field(entry.getKey())
                                    .matching(values.next()));
                        }

                        return step;
                    }
            ).fetchAll();

        }else{
            //more complicated version probably need to make an AST
            return null;
        }



    }
    @Override
    protected List<ControlledVocabulary> indexSearch(String query, Optional<Integer> top, Optional<Integer> skip, Optional<Integer> fdim) {
        SearchSession session = searchService.createSearchSession();
        List<ControlledVocabulary> dslHits = parseQueryIntoMatch(query , session).hits();


       System.out.println("dslHits = " + dslHits);
        return dslHits;
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

    private ControlledVocabulary adaptSingleRecord(JsonNode cvValue) throws IOException {
        try {
            String domain = cvValue.at("/domain").asText();
            JsonNode vtype = cvValue.at("/vocabularyTermType");
            String termType = null;
            System.out.println("cvValue = " + cvValue);
            System.out.println("vType =  " + vtype);
            if (!vtype.isTextual()) {
                ObjectNode objn = (ObjectNode) cvValue;
                //Sometimes stored as an object, instead of a text value
                objn.set("vocabularyTermType", cvValue.at("/vocabularyTermType/value"));
            }

            termType = cvValue.at("/vocabularyTermType").asText();

            ControlledVocabulary cv = (ControlledVocabulary) objectMapper.treeToValue(cvValue, objectMapper.getClass().getClassLoader().loadClass(termType));
            //if there was an ID with this object, get rid of it
            //it was added by mistake
            cv.id = null;

            if (cv.terms != null) { //Terms can be null sometimes now
                for (VocabularyTerm vt : cv.terms) {
                    vt.id = null;
                }
            }

            cv.setVocabularyTermType(getCVClass(domain).getName());
            return cv;
        }catch(ClassNotFoundException e){
            throw new IOException("error creating Controlled Vocabulary instance", e);
        }
    }

    private static Class<? extends ControlledVocabulary> getCVClass (String domain){


        if(fragmentDomains.contains(domain)){
            return FragmentControlledVocabulary.class;
        }else if(codeSystemDomains.contains(domain)){
            return CodeSystemControlledVocabulary.class;
        }else{
            return ControlledVocabulary.class;
        }
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

}
