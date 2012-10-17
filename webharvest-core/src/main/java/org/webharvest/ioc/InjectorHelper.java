package org.webharvest.ioc;

import com.google.inject.Inject;
import com.google.inject.Injector;

// TODO Missing documentation
// TODO Missing unit test
public final class InjectorHelper {

     @Inject private static Injector injector;

     private InjectorHelper() {
         // DO nothing constructor
     }

     // TODO Missing documentation
     // TODO Missing unit test
     public static Injector getInjector() {
         return injector;
     }

}
