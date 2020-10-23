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
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.hibernate.search.backend.lucene.LuceneExtension;
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

    @Override
    protected List<ControlledVocabulary> indexSearch(String query, Optional<Integer> top, Optional<Integer> skip, Optional<Integer> fdim) {
        SearchSession session = searchService.createSearchSession();
        List<ControlledVocabulary> dslHits =           session.search(ControlledVocabulary.class)
                    .where( f -> f.match()
                            .fields( "Domain" )
                            .matching( "ACCESS_GROUP" )
                    )
                .fetchAllHits();


       System.out.println("dslHits = " + dslHits);

        String[] fields = parseFieldsFrom(query);
        QueryParser parser;
        if(fields.length==1){
            parser = new QueryParser(fields[0], new KeywordAnalyzer());
        }else{
            parser = new MultiFieldQueryParser(fields, new KeywordAnalyzer());
        }
        System.out.println("parsed fields =" + Arrays.toString(fields));
        List<ControlledVocabulary> hits = session.search( ControlledVocabulary.class )
                .extension( LuceneExtension.get() )
                .where( f -> {
                    try {
                        return f.fromLuceneQuery( parser.parse(query) );
                    } catch (ParseException e) {
                        return Sneak.sneakyThrow(new RuntimeException(e));
                    }
                })
                .fetchHits(skip.orElse(null), top.orElse(null));

        System.out.println("found # hits = " + hits.size());
        return hits;

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
