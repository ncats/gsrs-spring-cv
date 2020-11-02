package gsrs;

import ix.core.search.text.IndexValueMaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class IndexValueMakerConfig {

    @Autowired
    private List<IndexValueMaker> indexValueMakers;


}
