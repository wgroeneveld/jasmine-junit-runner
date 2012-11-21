
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
 * 2) Fix CSS2Properties support for parsing style attributes: get from raw node context.
 * 3) Fix CSS2Properties support for setting values: all properties have the same objmaps, wtf?
 * 4) Fix focus() which sets document.activeElement correctly for jQuery:focus
 * 5) Fix Input click() behavior for checkboxes. Warning: jQ's click() <-> DOM's click (checked value too late set)!
 **/
(function() {

	var oldEnvjsUriFn = Envjs.uri;
	Envjs.uri = function(path, baseDir) {
		if(baseDir) {
			return oldEnvjsUriFn(path, baseDir);
		}
		return oldEnvjsUriFn(path, "file:///" + ("" + Envjs.getcwd()).replace(/\\/g, '/') + "/");
	};
	
	(function(Element) {
	
		var style = "style";
		function lookupStyleInNodeAttributes(el) {
			if(el.attributes) {
				for(var i = 0; i < el.attributes.length; i++) {
					if(el.attributes[i].nodeName === style) {
						return el.attributes[i].nodeValue;
					}
				}
			}
		}

		var styleSetFn = Element.__lookupGetter__(style);		
		Element.__defineGetter__(style, function() {	
			if(!this.cssText) {
				this.cssText = lookupStyleInNodeAttributes(this);
			}
			return styleSetFn.apply(this);
		});
	
	})(HTMLElement.prototype);

	 (function(input) {
         var oldClick = input.prototype.click;
         input.prototype.click = function() {
             if(this.type === "checkbox") {
                 this.checked = !this.checked;
             }
             oldClick.apply(this, arguments);
         }
     })(HTMLInputElement);
	
    (function(Input, Textarea, document) {
        var activeElement;
        function fixFocusForPrototype(element) {
            var originalFocus = element.prototype.focus;
            element.prototype.focus = function(element) {
                activeElement = this;
                originalFocus.apply(this, arguments);
            }
        }

        fixFocusForPrototype(Input);
        fixFocusForPrototype(Textarea);
        
        document.__defineGetter__("activeElement", function() {
            return activeElement;
        });
        
    })(HTMLInputElement, HTMLTextAreaElement, document);
	
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

/**
 * Envjs timeout fixes which use native Java code to re-implement setTimeout and setInterval
 * also sets clearTimeout & clearInterval on same level. 
 */
(function() {
	var threadTimeoutPool = {};

	window.setTimeout = function(closure, timeout) {
	    var thread = spawn(function() {
	        try {
	            java.lang.Thread.sleep(timeout);    
	            closure();
	        } catch(e) {
	            // ignore InterruptedExceptions, is probably due to clearTimeout
	            if (!(e.javaException instanceof java.lang.InterruptedException)) {
	                throw(e);
	            }
	        }
	    });
	    
	    threadTimeoutPool[thread.getId()] = thread;
	    return thread.getId();
	};

	window.setInterval = function(closure, timeout) {
	    var thread = spawn(function() {
	        try {            
	            while(true) {
	                java.lang.Thread.sleep(timeout);
	                closure();
	            }
	        } catch(e) {
	            // ignore InterruptedExceptions, is probably due to clearTimeout
	            if (!(e.javaException instanceof java.lang.InterruptedException)) {
	                throw(e);
	            }
	        }
	    });
	    
	    threadTimeoutPool[thread.getId()] = thread;
	    return thread.getId();
	};

	window.clearTimeout = function(threadId) {
	    if (threadId) {
	        if(threadTimeoutPool[threadId]) {
	            threadTimeoutPool[threadId].interrupt();
	            delete threadTimeoutPool[threadId];
	        }
	    }
	};

	window.clearInterval = window.clearTimeout;	
})();
