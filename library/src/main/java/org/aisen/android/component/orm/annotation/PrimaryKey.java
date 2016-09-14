package org.aisen.android.component.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME) 
public @interface PrimaryKey {
	
	 public String column() default "primary_key";
	 
}
