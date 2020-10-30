package gsrs.repository;



import ix.ginas.models.v1.ControlledVocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ControlledVocabularyRepository extends GsrsRepository<ControlledVocabulary, Long> {

    List<ControlledVocabulary> findByDomain(String domain);
    @Query(value = "select * from ControlledVocabulary  where id=:id", nativeQuery = true)
    List<ControlledVocabulary> foo(String id);

}
