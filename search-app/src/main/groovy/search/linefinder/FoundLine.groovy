package search.linefinder

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@CompileStatic
@ToString(includeFields = true, includePackage = false, ignoreNulls = true)
@EqualsAndHashCode(includeFields = true)
class FoundLine {

	public String line

	public int lineNr

	public List<String> contextLinesBefore = []

	public List<String> contextLinesAfter = []

	public boolean contextLinesBeforeOverflow

	public boolean contextLinesAfterOverflow
}
