(function() {

    if (!jasmine) {
        throw new Exception("jasmine library does not exist in global namespace!");
    }
	
    /**
     * Hooks up into the JUnit TestRunner system to allow Jasmine tests to run in Eclipse!
	 * Also sets a "done" flag on the spec itself since there is nothing like it in Jasmine
     */
    var DelegatorJUnitReporter = function() {
		this.javaReporter = jasmine.DelegatorJUnitReporter.javaReporter;
	};
	
	DelegatorJUnitReporter.prototype = {
        reportRunnerStarting: function(runner) {
			if(this.javaReporter) {
				this.javaReporter.reportRunnerStarting(runner);
			}
        },

        reportSpecStarting: function(spec) {
			spec.done = false;
			
			if(this.javaReporter) {
				this.javaReporter.reportSpecStarting(spec);
			}
        },

        reportSpecResults: function(spec) {
			spec.done = true;
			
			if(this.javaReporter) {
				this.javaReporter.reportSpecResults(spec);
			}
        },

        reportSuiteResults: function(suite) {
			if(this.javaReporter) {
				this.javaReporter.reportSuiteResults(suite);
			}
        },

        reportRunnerResults: function(runner) {
			if(this.javaReporter) {
				this.javaReporter.reportRunnerResults(runner);
			}
        }
    };

    // export public
    jasmine.DelegatorJUnitReporter = DelegatorJUnitReporter;
})();
