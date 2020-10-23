package com.kotlin.nav_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * create by max at 2020/10/23 10:51
 */
@Target(ElementType.TYPE)
public @interface Destination {

    String pageUrl();

    boolean asStarter() default false;
}
