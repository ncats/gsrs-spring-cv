package gsrs;

import com.example.demo.Author;
import com.example.demo.Book;
import com.example.demo.BookSearchService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gsrs.repository.ControlledVocabularyRepository;
import ix.ginas.models.v1.ControlledVocabulary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import java.io.InputStream;
import java.util.List;

@Profile("!test")
@Component
public class LoadCvOnStartup implements ApplicationRunner {



    @Autowired
    private ControlledVocabularyRepository repository;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${gsrs.cv.jsonFile}")
    private String jsonPath;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        System.out.println("RUNNING");
        System.out.println("reading property file at path '"+jsonPath + "'");
        JsonNode json;
        try(InputStream in = getClass().getResourceAsStream(jsonPath)){
            json = objectMapper.readValue(in, JsonNode.class);

        }

        System.out.println(json);

        List<ControlledVocabulary> cv = CvUtils.adaptList(json, objectMapper);
        repository.saveAll(cv);
        repository.flush();



    }
}
