package be.klak.rhino;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

import org.apache.commons.io.FilenameUtils;

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
		try {
			List<Path> files = getFiles(path, jsFiles);
			for (Path file: files) {
				load(file.toString());
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private List<Path> getFiles(String root, String... globs) throws IOException {
		// Use a priority queue as the order of the files may make a difference
		final PriorityQueue<MatchResult> matchResults = new PriorityQueue<>();
		if (Paths.get(root).toFile().exists()) {
			final List<PathMatcher> pathMatchers = getPathMatchers(root, globs);
			Files.walkFileTree(Paths.get(root), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					MatchResult matchResult = match(file, pathMatchers);
					if (matchResult != null) {
						matchResults.add(matchResult);
					};
					return super.visitFile(file, attrs);
				}
			});
		}
		List<Path> paths = new ArrayList<>();
		for (MatchResult matchResult : matchResults) {
			paths.add(matchResult.path);
		}
		return paths;
	}

	private List<PathMatcher> getPathMatchers(String root, String[] globs) {
		List<PathMatcher> matchers = new ArrayList<>();
		for (String glob : globs) {
			matchers.add(FileSystems.getDefault().getPathMatcher("glob:" + root + glob));
		}
		return matchers;
	}


	private MatchResult match(Path file,
			List<PathMatcher> pathMatchers) {
		MatchResult matchResult = null;
		for (int i = 0; i < pathMatchers.size() && matchResult == null; i++) {
			if (pathMatchers.get(i).matches(file)) {
				matchResult = new MatchResult(i, file);
			}
		}
		return matchResult;
	}

	private void load(String fileName) {
		String path = FilenameUtils.separatorsToUnix(fileName);
		evalJS("load('" + path + "')");
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
		loadFromClasspath("js/lib/env.rhino.1.2.js");
		loadFromClasspath("js/lib/env.utils.js");
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

	private static class MatchResult implements Comparable<MatchResult> {

		private final int position;
		private final Path path;

		public MatchResult(int position, Path path) {
			this.position = position;
			this.path = path;
		}

		@Override
		public int compareTo(MatchResult o) {
			return position - o.position;
		}

	}
}
