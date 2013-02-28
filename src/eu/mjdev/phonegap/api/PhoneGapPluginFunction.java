package eu.mjdev.phonegap.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PhoneGapPluginFunction {
	String jscode() default "{fncName}:function({fncparams}cbsuccess,cberror){$.app.exec(cbsuccess,cberror,'{pluginName}','{fncName}',[{fncparams}]);}";

	String help() default "";

	String[] params() default {};
}