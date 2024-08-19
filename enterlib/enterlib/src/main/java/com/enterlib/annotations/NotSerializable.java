/**
 *
 */
package com.enterlib.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.enterlib.serialization.IStringSerializer;

/**
 * @author ansel Used to mark fields that are not serializable when using using
 *         a {@link IStringSerializer}.
 *
 */

@Retention(RetentionPolicy.RUNTIME)
// @Target(ElementType.FIELD)
public @interface NotSerializable {

}
