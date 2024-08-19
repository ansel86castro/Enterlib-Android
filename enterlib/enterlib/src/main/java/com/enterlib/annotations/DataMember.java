/**
 *
 */
package com.enterlib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.enterlib.serialization.IStringSerializer;

/**
 * @author ansel Decorate methods with this annotation to serialize their values
 *         when using a {@link IStringSerializer}
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface DataMember {

	public Class<?> listType();
}
