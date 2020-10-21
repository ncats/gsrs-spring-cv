package gsrs.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gsrs.repository.ControlledVocabularyRepository;
import ix.ginas.models.v1.CodeSystemControlledVocabulary;
import ix.ginas.models.v1.ControlledVocabulary;
import ix.ginas.models.v1.FragmentControlledVocabulary;
import ix.ginas.models.v1.VocabularyTerm;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@GsrsRestApiController(context ="vocabularies",  idHelper = IdHelpers.NUMBER)
public class CvController extends GsrsEntityController<ControlledVocabulary, Long> {

    private static Pattern NUMBER_PATTERN = Pattern.compile("^"+ IdHelpers.NUMBER.getRegexAsString()+"$");
    private static Set<String> fragmentDomains;
    private static Set<String> codeSystemDomains;
    static {
        fragmentDomains = new HashSet<String>();
        fragmentDomains.add("NUCLEIC_ACID_SUGAR");
        fragmentDomains.add("NUCLEIC_ACID_LINKAGE");
        fragmentDomains.add("NUCLEIC_ACID_BASE");
        fragmentDomains.add("AMINO_ACID_RESIDUE");

        codeSystemDomains = new HashSet<String>();
        codeSystemDomains.add("CODE_SYSTEM");
        codeSystemDomains.add("DOCUMENT_TYPE");
    }

    @Autowired
    private ControlledVocabularyRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    Long parseIdFromString(String idAsString) {
        return Long.parseLong(idAsString);
    }

    @Override
    public ControlledVocabulary fromJson(JsonNode json) throws IOException {
        return adaptSingleRecord(json);

    }

    @Override
    public List<ControlledVocabulary> fromJsonList(JsonNode list) throws IOException {
        List<ControlledVocabulary> adaptedCvs = new ArrayList<>(list.size());
        for(JsonNode cvValue: list){

            ControlledVocabulary cv = adaptSingleRecord(cvValue);
            //the Play version called cv.save() here we won't
            adaptedCvs.add(cv);


        }
        return adaptedCvs;
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
    public JsonNode toJson(ControlledVocabulary controlledVocabulary) throws IOException {
        return objectMapper.valueToTree(controlledVocabulary);
    }

    @Override
    public ControlledVocabulary create(ControlledVocabulary controlledVocabulary) {
        return repository.save(controlledVocabulary);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public Optional<ControlledVocabulary> get(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<ControlledVocabulary> flexLookup(String someKindOfId) {
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
