// shim to allow jasmine to run in Rhino without EnvJS loaded
// inspired by
//     https://groups.google.com/d/msg/jasmine-js/waor9RXUsDw/LY6gNLcMxkcJ
// implementation stolen from env.utils.js

(function(global) {
    var threadTimeoutPool = new java.util.HashMap();

    global.setTimeout = function(closure, timeout) {
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
        
        threadTimeoutPool.put(thread.getId(), thread);
        return thread.getId();
    };

    global.setInterval = function(closure, timeout) {
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
        
        threadTimeoutPool.put(thread.getId(), thread);
        return thread.getId();
    };

    global.clearTimeout = function(threadId) {
        if (threadId) {
            if(threadTimeoutPool.containsKey(threadId)) {
                threadTimeoutPool.remove(threadId).interrupt();
            }
        }
    };

    global.clearInterval = global.clearTimeout; 
})(this);
