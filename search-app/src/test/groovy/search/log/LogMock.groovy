package search.log

class LogMock {

	static ILog get() {
		[info : { message -> },
		 debug: { message -> },
		 warn : { message -> },
		 fatal: { message -> }] as ILog
	}
}
