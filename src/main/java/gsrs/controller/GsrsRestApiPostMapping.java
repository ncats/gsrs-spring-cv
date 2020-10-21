package gsrs.controller;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@RequestMapping(
        method = {RequestMethod.POST}
)
public @interface GsrsRestApiPostMapping {
    @AliasFor(annotation = RequestMapping.class, value = "value")
    String[] value() default "";

    String idPlaceholder() default "$ID";
}
