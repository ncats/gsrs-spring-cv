package gsrs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties("ix.api")

public class RegisteredFunctionProperties {

    private List<String> registeredfunctions;


    public List<String> getRegisteredfunctions() {
        return registeredfunctions;
    }

    public void setRegisteredfunctions(List<String> registeredfunctions) {
        this.registeredfunctions = registeredfunctions;
    }
}
