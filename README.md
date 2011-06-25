# Jasmine Junit Runner

## What's this? 

Quite simple, it's a custsom Java Junit Runner that allows you to embed Javascript Unit tests (using Jasmine) in your Java-based projects. It fully integrates with your most beloved IDE, your most hated version control system and of course your most needed CI env. 

So let's rephrase:

* Run Javascript (the <a href="https://github.com/pivotal/jasmine" target="_blank">Jasmine</a> - behavior driven - way) "specs" in Java
* Talks like a duck-erhm, any other Junit Java test. Just use a custom annotation (see below)
* Executes super-fast. No browser required. Hocus-pocus. (Rhino + Envjs magic)

## Does this thing generate Junit XML?

Yes and no. Not explicitly using the Jasmine Junit XML Reporter, but since it's a Java Junit Result, your build process will do that for you. 
Maven surefire plugins will generate the needed result files, for Jenkins to pick up. Your stacktrace/failure message will be something like:

> Expected x to be y (zz.js, #458)

Just like the default Jasmine HTML reporter.

## What Do I need to do? 

1. Fork this project. 
2. Create some Jasmine specs, place them in some folder.
3. Create a Junit test class, annotate it with _@RunWith(JasmineTestRunner.class)_
4. Fill in the blanks using @JasmineSuite

## More options

_@JasmineSuite_ allows you to set these options:

* debug: use the built-in Rhino debugger (gives you the chance to set a breakpoint before firing the test suite)
* jsRootDir: the javascript install root dir. Jasmine and other should be installed here (see source)
* sourcesRootDir: your production JS files root dir.
* specs: one or more spec file to run. Default behavior: use java Class name (replaces Test with Spec, see example)
* sources: one or more JS production file which your spec needs (included before specs, d'uh)
* generateSpecRunner: (the HTML output, useful for firefox/firebug debugging etc)

## Requirements

Currently, Jasmine Junit Runner relies on Rhino 1.7R2 (+ es5-shim) & Envjs 1.2 to interpret JS code. It also uses Jamsine 1.0.2 to read your spec files. All js libs are located in test/javascript/lib .

### Dependencies Overview

See the _pom.xml_ (Maven2) - you can build the whole thing using:

> mvn clean install  

* Rhino 1.7R2 + es5-shim 0.0.4 (not needed if you'll be using 1.7R3)
* Envjs 1.2 + required hacks in env.utils.js
* Jasmine 1.0.2
* Java libs: commons-io and commons-lang (test libs: mockito and fest assert)
 
* * *

# Examples

## Running a spec file as a Junit test

### Use the default spec naming convention

If you do not specify _specs_ with the annotation, the runner will auto-pick the spec name using your test class.
The below test will load _myAwesomeSpec.js_ from the specs dir (jsRootDir + '/specs/').

```java
@RunWith(JasmineTestRunner.class)
@JasmineSuite(sources = { 'jQuery.js', 'myAwesomeCode.js' } )
public class MyAwesomeTest {
}
```

your awesome production code relies on jQuery (of course it does), so you'll have to include it.

Your spec file might look like this:

```javascript
describe("my awesome code", function() {
	it("will always run", function() {
		expect(stuff.DoCoolThings()).toBe("awesome");
	});
});
```

### Using Junit's _@Before_ and _@After_ 

It's possible to do some extra work before and after each spec run:

```java
@RunWith(JasmineTestRunner.class)
@JasmineSuite
public class MyAwesomeTest {

  @Before
  public void beforeStuff(RhinoContext context) {
    context.evalJS("var prefabVar = { cool: 'yeah!' };");
  }
  
  @Before
  public void beforeStuffNoContext() {
    System.out.println("I'm gonna blow! Or Will I?");
  }
  
  @After
  public void afterStuff() {
    // say cool things
  }

}
```

What's happening?

* You can define n number of _PUBLIC_ methods annotated with @Before or @After
* You can, but don't have to, take the _RhinoContext_ object as the only parameter. This allows you to set stuff up in JS space before running the spec.

### Generating a spec runner

Your awesome test (example 1) would for instance generate this html file:

```html
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<title>Jasmine Test Runner</title>
		<link rel="stylesheet" type="text/css" href="./../lib/jasmine-1.0.2/jasmine.css">
		<script type="text/javascript" src="./../lib/jasmine-1.0.2/jasmine.js"></script>
		<script type="text/javascript" src="./../lib/jasmine-1.0.2/jasmine-html.js"></script>
		
		<script type='text/javascript' src='./../../../main/webapp/js/jquery.js'></script>
		<script type='text/javascript' src='./../../../main/webapp/js/myawesomecode.js'></script>
		
		<script type='text/javascript' src='./../specs/myawesomespec.js'></script>
	</head>
	<body>

		<script type="text/javascript">		
			jasmine.getEnv().addReporter(new jasmine.TrivialReporter());
			jasmine.getEnv().execute();
		</script>
	</body>
</html>
```

You can inspect the output using firefox, or debug in your spec file using firebug.

### Debugging in Java

When the debug mode flag has been set to _true_, you can use the <a href="http://www.mozilla.org/rhino/debugger.html" target="_blank">Rhino Debugger</a> to set breakpoints.
After pressing "GO", the tests will run and you can inspect stuff and step through the code.

Integrated debugging into for example Eclipse does not work for the moment. 

* * *

# Advanced: Implementation details

## RhinoContext API

The _RhinoContext_ class is basically a wrapper/facade/whatever which allows you to easily manipulate the Javascript scope. 
Read <a href="https://developer.mozilla.org/En/Rhino_documentation/Scopes_and_Contexts" target="_blank">Rhino docs: scopes and contexts</a> first please!

Creating a new RhinoContext initializes one "root" scope (toplevel), and assignes one context to the current Thread. 

### Evaluating async javascript code

Creating another RhinoContext while passing the root scope, uses prototypal inheritance to create a new toplevel scope. This means the root scope is shared across different contexts (and thus different threads). 
You can execute the _runAsync_ method, which does this:

* create a new thread and thus a new context
* create a new scope based on the root one -> shared
* execute stuff in the new scope (You can access root JS functions but not modify them, remember prototypal inheritance!)
* cleanup

For example, JasmineSpec uses the _execute_ Jasmine JS function on a spec and calls it in another thread:

```java
        baseContext.runAsync(new RhinoRunnable() {

            @Override
            public void run(RhinoContext context) {
			    // get some random spec from Jasmine
			    NativeObject someSpec = (NativeObject) context.evalJS("jasmine.getEnv().currentRunner().suites()[0].specs()[0]");
                context.executeFunction(someSpec, "execute");
            }
        });
```

### Creating a Rhino debugger
 
Basically creates a _org.mozilla.javascript.tools.debugger.Main_ object. Pitfall: create before loading all required JS files, but after creating the rhino context!
To acutally break once (so users can set breakpoints and press GO), use this:

> debugger.doBreak();	

### Executing functions

_executeFunction_ is a convenience method to call a function on a passed NativeObject. The function pointer may reside in the object's prototype, you don't need to explicitly check this in Javascript but you do using Rhino! 

## Envjs Utils/Hacks

### Error.stack fix

In firefox, you can get a stacktrace from a JS exception using:

> new Error("BOOM").stack

Of course this does not work in Envjs. But Rhino attaches an internal _rhinoException_ to each JS Error object, so using a bit of magic, now it's possible to call _getStackTrace()_

### Envjs.uri Windows relative paths fix

Use _file:///_ (three forward slashes) if no context has been provided. Works like this:

```javascript
Envjs.uri(path, "file:///" + ("" + Envjs.getcwd()).replace(/\\/g, '/') + "/")
```

### window.setTimeout fix

Used by Jasmine internally for async spec execution, but for some reason the Envjs Javascript implementation is broken. 
A simple fix is possible, since using Rhino you can call Java objects in Javascript space! Wow awesome. So just create a new thread and use _sleep_:

```javascript
	window.setTimeout = function(closure, timeout) {
		spawn(function() {
			java.lang.Thread.sleep(timeout);
			closure();
		});
	};
```
