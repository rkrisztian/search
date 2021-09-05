package search.annotations

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Similar to the one in Guava, but adding a whole library just for that would have been an overkill.
 */
@Retention(RetentionPolicy.SOURCE)
@interface VisibleForTesting {

}
