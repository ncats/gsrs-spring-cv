package gsrs.indexer;

import ix.ginas.models.v1.ControlledVocabulary;
import org.springframework.stereotype.Service;

@Service
public class CvSearchService extends EntitySearchService<ControlledVocabulary> {
    public CvSearchService() {
        super(ControlledVocabulary.class);
    }
}
