package gsrs.controller;

import gov.nih.ncats.common.util.CachedSupplier;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix="gsrs.api")
@Data
public class GsrsControllerConfiguration {

    private String errorCodeParameter;

    private Integer forceErrorCodeValue;

    private static boolean isValidErrorCode(int askedForStatus) {
        return askedForStatus >=400 && askedForStatus< 600;
    }
    public int overrideErrorCodeIfNeeded(int defaultStatus, WebRequest request){
        String value =request.getParameter(errorCodeParameter);
        return overrideErrorCodeIfNeeded(defaultStatus, Collections.singletonMap(errorCodeParameter, value));
    }
    public int overrideErrorCodeIfNeeded(int defaultStatus, Map<String, String> queryParameters){
        //GSRS-1598 force not found error to sometimes be a 500 instead of 404
        //if requests tells us
        try {
            String specifiedResponse = queryParameters.get(errorCodeParameter);
            if(specifiedResponse !=null){
                int askedForStatus = Integer.parseInt(specifiedResponse);
                //status must be a 4xx or 5xx so people can't make it 200
                if(isValidErrorCode(askedForStatus)){
                    return askedForStatus;
                }
            }

        }catch(Exception e){
            //no request?
        }

        if(forceErrorCodeValue!=null){
            int asInt = forceErrorCodeValue.intValue();
            if(isValidErrorCode(asInt)){
                return asInt;
            }
        }
        //use default
        return defaultStatus;
    }

    public ResponseEntity<Object> handleNotFound(Map<String, String> queryParameters){
        int status = overrideErrorCodeIfNeeded(404, queryParameters);
        return new ResponseEntity<>( createStatusJson("not found", status), HttpStatus.valueOf(status));

    }
    public ErrorInfo createErrorStatusBody(Throwable t, int status,  WebRequest request){
        int statusToUse = overrideErrorCodeIfNeeded(status, request);
        Object body = createStatusJson("not found", statusToUse);
        return ErrorInfo.builder()
                            .body(body)
                            .status(HttpStatus.valueOf(statusToUse))
                            .build();


    }

    @Data
    @Builder
    public static class ErrorInfo{
        private Object body;
        private HttpStatus status;
    }

    private static Object getError(Throwable t, int status){

        Map m=new HashMap();
        if(t instanceof InvocationTargetException){
            m.put("message", ((InvocationTargetException)t).getTargetException().getMessage());
        }else{
            m.put("message", t.getMessage());
        }
        m.put("status", status);
        return m;
    }

    private static Object createStatusJson(String message, int status){
        Map m=new HashMap();
        m.put("message", message);

        m.put("status", status);
        return m;
    }
}
