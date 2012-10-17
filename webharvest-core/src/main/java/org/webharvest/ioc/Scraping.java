package org.webharvest.ioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// TODO Missing documentation
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
public @interface Scraping {

}
