package search.conf

trait Dumpable {

	@Override
	String toString() {
		this.dump()
	}
}
