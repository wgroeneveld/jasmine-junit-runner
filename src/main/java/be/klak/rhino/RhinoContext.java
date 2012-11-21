package be.klak.rhino;

import java.net.URL;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.shell.Global;

public class RhinoContext {

	private Context jsContext;
	private Scriptable jsScope;

	public RhinoContext() {
		this.jsContext = createJavascriptContext();
		this.jsScope = createJavascriptScopeForContext(this.jsContext);
	}

	public RhinoContext(Scriptable sharedScope) {
		this.jsContext = createJavascriptContext();
		Scriptable newScope = this.jsContext.newObject(sharedScope);
		newScope.setPrototype(sharedScope);
		newScope.setParentScope(null);

		this.jsScope = newScope;
	}

	private RhinoContext createNewRhinoContextBasedOnPrevious() {
		return new RhinoContext(this.jsScope);
	}

	public void runAsync(final RhinoRunnable runnable) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				RhinoContext newRhinoContextBasedOnPrevious = createNewRhinoContextBasedOnPrevious();
				try {
					runnable.run(newRhinoContextBasedOnPrevious);
				} finally {
					newRhinoContextBasedOnPrevious.exit();
				}
			}
		}).start();
	}

	public Object evalJS(String js) {
		return this.jsContext.evaluateString(this.jsScope, js, "script", 1, null);
	}

	@SuppressWarnings("unchecked")
	public <T extends ScriptableObject> T createClassInJS(Class<T> classToExport) {
		exportClass(classToExport);
		T newObj = (T) jsContext.newObject(jsScope, classToExport.getSimpleName());
		return newObj;
	}

	public void setProperty(String objectToReceiveProperty, String property, Object value) {
		Object obj = evalJS(objectToReceiveProperty);
		if (obj == null || !(obj instanceof ScriptableObject)) {
			throw new IllegalStateException("object to receive property is no ScriptableObject but a "
					+ (obj == null ? "" : obj.getClass().getSimpleName()));
		}

		ScriptableObject objectToReceive = (ScriptableObject) obj;
		objectToReceive.put(property, objectToReceive, value);
	}

	private void exportClass(Class<? extends ScriptableObject> classToExport) {
		try {
			ScriptableObject.defineClass(this.jsScope, classToExport);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void load(String path, String... jsFiles) {
		for (String jsFile : jsFiles) {
			load(path + jsFile);
		}
	}

	public void load(String fileName) {
		evalJS("load('" + fileName + "')");
		// Main.processFile(this.jsContext, this.jsScope, fileName);
	}

	// {{{ loadFromClasspath
	/**
	 * Loads a resource from the classpath.
	 *
	 * @param resource the resource to resolve from the classpath
	 */
	public void loadFromClasspath(final String resource) {
	    URL rsrcUrl =
	    	Thread.currentThread().getContextClassLoader().getResource(resource);

	    if (rsrcUrl == null) {
	    	throw new IllegalArgumentException("resource " + resource + " not found on classpath");
	    }

	    evalJS(String.format("load('%s')", rsrcUrl.toExternalForm()));
	}
	// }}}

	public Object executeFunction(ScriptableObject object, String fnName, Object[] arguments) {
		Object fnPointer = object.get(fnName, object);
		if (fnPointer == null || !(fnPointer instanceof Function)) {
			fnPointer = object.getPrototype().get(fnName, object);
		}

		return ((Function) fnPointer).call(jsContext, jsScope, object, arguments);
	}

	public Object executeFunction(ScriptableObject object, String fnName) {
		return executeFunction(object, fnName, new Object[] {});
	}

	public Context getJsContext() {
		return jsContext;
	}

	public Scriptable getJsScope() {
		return jsScope;
	}

	public void loadEnv(String jsDir) {
		load(jsDir + "/lib/env.rhino.1.2.js");
		load(jsDir + "/lib/env.utils.js");
		load(jsDir + "/envJsOptions.js");
	}

	private Global createJavascriptScopeForContext(Context jsContext) {
		Global scope = new Global();
		scope.init(jsContext);
		return scope;
	}

	private Context createJavascriptContext() {
		Context jsContext = ContextFactory.getGlobal().enterContext();
		jsContext.setOptimizationLevel(-1);
		jsContext.setLanguageVersion(Context.VERSION_1_8);
		jsContext.setErrorReporter(new ChainedErrorReporter(jsContext.getErrorReporter()));
		return jsContext;
	}

	public void exit() {
		Context.exit();
	}
}
