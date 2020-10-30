package gsrs;



import gsrs.controller.CvController;
import gsrs.repository.ControlledVocabularyRepository;
import gsrs.repository.GroupRepository;
import ix.IxMarker;
import ix.core.models.Group;
import ix.ginas.models.v1.ControlledVocabulary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableConfigurationProperties
@EnableWebSecurity
@EnableJpaRepositories(basePackageClasses = { ControlledVocabularyRepository.class, GroupRepository.class})
@ComponentScan(basePackageClasses = {LuceneSpringDemoApplication.class, GroupRepository.class,  IxMarker.class,  CvController.class})
@EntityScan(basePackageClasses = {Group.class, ControlledVocabulary.class})
public class LuceneSpringDemoApplication {



    public static void main(String[] args) {
        SpringApplication.run(LuceneSpringDemoApplication.class, args);
    }

}
