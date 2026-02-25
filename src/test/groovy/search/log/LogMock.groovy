package search.log

class LogMock {

	static Log get() {
		[
				info: { message -> },
				debug: { message -> },
				warn: { message -> },
				rawPrintln: { message -> }
		] as Log
	}

}
