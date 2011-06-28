
/**
 * Rhino and Envjs additions, filling the missing pieces such as a decent stacktrace
 * 1) Firefox knows new Error().stack but Envjs does not.
 */
(function() {
	var NEWLINE = "\r\n";

	function manuallyTriggerException() {
		var manuallyTriggeredExMessage = "";			  
		try {
			OEIWABEZIG
		} catch(e) {
			// TODO knip- en plakwerk: de eerste lijn moet natuurlijk weg etc...
			return e;
		}
	}
	
	function stripRhinoSpecificExceptionLines(stack) {
		var ignoreTracingInStackFor = [ "env.utils.js", "org.mozilla.javascript" ];

		return stack.split(NEWLINE).filter(function(stackLine) {
			return ignoreTracingInStackFor.every(function(ignoreThisInTrace) {
				return stackLine.indexOf(ignoreThisInTrace) < 0;
			});
		}).reduce(function(prev, curr) {
			return prev + NEWLINE + curr;
		});
	}
		
	window.Error.writeStackTrace = function(e) {
		var stringWriter = new java.io.StringWriter();
		var printWriter = new java.io.PrintWriter(stringWriter);
		e.rhinoException.printStackTrace(printWriter);
		return stringWriter.toString() + "" // I don't want the java.lang.String object!
	};
	
	window.Error.prototype.getStackTrace = function() {
		// TODO add other stuff from Object.keys(new Error("bla")) (see Firebug)
		var trace = this.message + NEWLINE;
		if(this.stack) {
			trace += this.stack;
		} else if(this.rhinoException) {
			trace += window.Error.writeStackTrace(this.rhinoException);
		} else {
			trace += stripRhinoSpecificExceptionLines(window.Error.writeStackTrace(manuallyTriggerException()));
		}
		
		return trace;
	}

})();

/**
 * Envjs specific hacks
 * 1) Fix Envjs relative path system to work with Windows path systems
 * 2) Fix window.setTimeout() using Rhino specific functions
 * 3) Fix CSS2Properties support: all properties have the same objmaps, wtf?
 */
(function() {

	var oldEnvjsUriFn = Envjs.uri;
	Envjs.uri = function(path, baseDir) {
		if(baseDir) {
			return oldEnvjsUriFn(path, baseDir);
		}
		return oldEnvjsUriFn(path, "file:///" + ("" + Envjs.getcwd()).replace(/\\/g, '/') + "/");
	};
	
	window.setTimeout = function(closure, timeout) {
		spawn(function() {
			java.lang.Thread.sleep(timeout);
			closure();
		});
	};

	(function(css) {

		var setCssProperty = css.prototype.setProperty;
		css.prototype.setProperty = function(name, value) {
			// create a shallow clone of __supportedStyles__ (styleIndex' default value) if prototype not yet set
			if(Object.keys(Object.getPrototypeOf(this.styleIndex)).length === 0) {
				this.styleIndex = Object.create(this.styleIndex);
			}
			
			return setCssProperty.call(this, name, value);
		}
	})(CSS2Properties);
	
})();
