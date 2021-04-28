package gsrs;




import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@SpringBootApplication
@EnableConfigurationProperties
//@EnableEurekaClient
@EnableGsrsJpaEntities
@EnableGsrsApi
@EntityScan(basePackages ={"ix","gsrs", "gov.nih.ncats"} )
@EnableJpaRepositories(basePackages ={"ix","gsrs", "gov.nih.ncats"} )
//@EnableJpaRepositories(basePackageClasses = { ControlledVocabularyRepository.class, GroupRepository.class})
//@ComponentScan(basePackageClasses = {LuceneSpringDemoApplication.class, GroupRepository.class,  IxMarker.class,  CvController.class})
//@EntityScan(basePackageClasses = {Group.class, ControlledVocabulary.class})
@EnableGsrsLegacyAuthentication
@EnableGsrsLegacyCache
//@EnableSwagger2
public class LuceneSpringDemoApplication {



    public static void main(String[] args) {
        SpringApplication.run(LuceneSpringDemoApplication.class, args);
    }

}
