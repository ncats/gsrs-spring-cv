package gsrs.controller;

import gsrs.springUtils.AutowireHelper;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcRegistrations webMvcRegistrationsHandlerMapping() {
        System.out.println("here in webmvc registration");
        return new WebMvcRegistrations() {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {

                return new RequestMappingHandlerMapping() {
                    private final static String API_BASE_PATH = "api/v1";


                    @Override
                    protected boolean isHandler(Class<?> beanType) {
                        boolean result= AnnotatedElementUtils.hasAnnotation(beanType, GsrsRestApiController.class) /*|| super.isHandler(beanType)*/;
                        System.out.println("asking if is handler for " + beanType + "  " + result);

                        return result;
                    }

                    @Override
                    protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
                       //the handler is often a String for the package name... convert to real class
                        Class<?> handlerType = (handler instanceof String ?
                                obtainApplicationContext().getType((String) handler) : handler.getClass());

                        if (handlerType == null) {
                            return;
                        }
                            Class<?> beanType = ClassUtils.getUserClass(handlerType);

                        GsrsRestApiController gsrsRestApiAnnotation = AnnotationUtils.findAnnotation(beanType, GsrsRestApiController.class);
                        System.out.println("bean type" + beanType + "  annotation = " + gsrsRestApiAnnotation);

                        System.out.println(method);
                        GsrsRestApiGetMapping getMapping = method.getAnnotation(GsrsRestApiGetMapping.class);
                        GsrsRestApiPostMapping postMapping=null;
                        System.out.println("\t" + getMapping);
                        if(getMapping ==null){
                            //check for post
                            postMapping = method.getAnnotation(GsrsRestApiPostMapping.class);

                        }

                        //we want to do 2 things
                        //1. add the api base to everything
                        //2. if there's an ID use the regex for that entity type
                        PatternsRequestCondition apiPattern = new PatternsRequestCondition(API_BASE_PATH)
                                .combine(mapping.getPatternsCondition());

                        if (getMapping != null) {
                           if (gsrsRestApiAnnotation != null) {


                               CommonIDRegexes commonIdHelper = gsrsRestApiAnnotation.idHelper();
                               IdHelper idHelper;
                               if(commonIdHelper ==CommonIDRegexes.CUSTOM){
                                   String className = gsrsRestApiAnnotation.customIdHelperClassName();
                                   try {
                                       if(className ==null || className.isEmpty()){

                                           idHelper = gsrsRestApiAnnotation.customIdHelperClass().newInstance();


                                       }else{
                                           idHelper = (IdHelper) ClassUtils.forName(className, getClass().getClassLoader()).newInstance();
                                       }
                                   } catch (Exception e) {
                                       e.printStackTrace();
                                       throw new IllegalStateException("error instantiating idHelper class", e);
                                   }
                                   //inject anything if needed
                                   AutowireHelper.getInstance().autowire(idHelper);
                               }else{
                                   idHelper= commonIdHelper;
                               }

                               String idPlaceHolder = getMapping.idPlaceholder();
                               String notIdPlaceHolder = getMapping.notIdPlaceholder();
                               Set<String> updatedPatterns = new LinkedHashSet<>();
                               for(String route : apiPattern.getPatterns()) {
                                   String updatedRoute = idHelper.replaceId(route, idPlaceHolder);
                                   updatedRoute = idHelper.replaceInverseId(updatedRoute, notIdPlaceHolder);
                                   System.out.println("updated route : " + route + "  -> " + updatedRoute);
                                   updatedPatterns.add(updatedRoute);

                               }
                               apiPattern = new PatternsRequestCondition(updatedPatterns.toArray(new String[updatedPatterns.size()]));

                            }
                            mapping = new RequestMappingInfo(mapping.getName(), apiPattern,
                                    mapping.getMethodsCondition(), mapping.getParamsCondition(),
                                    mapping.getHeadersCondition(), mapping.getConsumesCondition(),
                                    mapping.getProducesCondition(), mapping.getCustomCondition());

                        }else if(postMapping !=null){
                                if (gsrsRestApiAnnotation != null) {


                                    CommonIDRegexes commonIdHelper = gsrsRestApiAnnotation.idHelper();
                                    IdHelper idHelper;
                                    if(commonIdHelper ==CommonIDRegexes.CUSTOM){
                                        String className = gsrsRestApiAnnotation.customIdHelperClassName();
                                        try {
                                            if(className ==null || className.isEmpty()){

                                                    idHelper = gsrsRestApiAnnotation.customIdHelperClass().newInstance();


                                            }else{
                                                idHelper = (IdHelper) ClassUtils.forName(className, getClass().getClassLoader()).newInstance();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            throw new IllegalStateException("error instantiating idHelper class", e);
                                        }
                                        //inject anything if needed
                                        AutowireHelper.getInstance().autowire(idHelper);
                                    }else{
                                        idHelper= commonIdHelper;
                                    }

                                    String idPlaceHolder = postMapping.idPlaceholder();
                                    String notIdPlaceHolder = postMapping.notIdPlaceholder();
                                    Set<String> updatedPatterns = new LinkedHashSet<>();
                                    for(String route : apiPattern.getPatterns()) {
                                        String updatedRoute = idHelper.replaceId(route, idPlaceHolder);
                                        updatedRoute = idHelper.replaceInverseId(updatedRoute, notIdPlaceHolder);
                                        System.out.println("updated route : " + route + "  -> " + updatedRoute);
                                        updatedPatterns.add(updatedRoute);

                                    }
                                    apiPattern = new PatternsRequestCondition(updatedPatterns.toArray(new String[updatedPatterns.size()]));

                                }
                                mapping = new RequestMappingInfo(mapping.getName(), apiPattern,
                                        mapping.getMethodsCondition(), mapping.getParamsCondition(),
                                        mapping.getHeadersCondition(), mapping.getConsumesCondition(),
                                        mapping.getProducesCondition(), mapping.getCustomCondition());

                            }

                        super.registerHandlerMethod(handler, method, mapping);

                    }

                    ;
                };
            }
        };

    }
}
