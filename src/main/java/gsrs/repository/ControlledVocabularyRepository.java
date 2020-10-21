package gsrs.repository;



import ix.ginas.models.v1.ControlledVocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ControlledVocabularyRepository extends JpaRepository<ControlledVocabulary, Long> {

    List<ControlledVocabulary> findByDomain(String domain);

}
