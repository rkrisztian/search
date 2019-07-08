package search.conf

import groovy.transform.CompileStatic

@CompileStatic
trait Dumpable {

	@Override
	String toString() {
		this.dump()
	}
}
