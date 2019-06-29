package search.linefinder

import search.conf.Dumpable

class FoundLine implements Dumpable {

	public String line

	public int lineNr

	public List<String> contextLinesBefore

	public List<String> contextLinesAfter

	public boolean contextLinesBeforeOverflow

	public boolean contextLinesAfterOverflow
}
