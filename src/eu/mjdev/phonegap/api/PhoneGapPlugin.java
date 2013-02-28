package eu.mjdev.phonegap.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PhoneGapPlugin {
	String appendTo();

	String jscode() default "";
}