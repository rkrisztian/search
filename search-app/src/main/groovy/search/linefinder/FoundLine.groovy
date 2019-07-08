package search.linefinder

import groovy.transform.CompileStatic
import search.conf.Dumpable

@CompileStatic
class FoundLine implements Dumpable {

	public String line

	public int lineNr

	public List<String> contextLinesBefore

	public List<String> contextLinesAfter

	public boolean contextLinesBeforeOverflow

	public boolean contextLinesAfterOverflow
}
