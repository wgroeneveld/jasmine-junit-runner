# Jasmine Junit Runner

## What's this? 

Something like this:

```javascript
describe("pure awesomeness", function() {
	it("should be amazing!", function() {
		expect(stuff).toEqual("amazing");
	});
	
	it("should be awesome", function() {
		expect(moreStuff).toBe("awesome");
	});
});

describe("coolness", function() {
	it("should be cooler than ice when freezed", function() {
		var coolness = CoolingRepository.beCool();
		coolness.freeze();
		expect(coolness.coolnessRatio).toBe(-100);
	});
	it("should be cool enough by default", function() {
		expect(CoolingRepository.beCool().coolnessRatio).toBe(-5);
	});
});
```

Being translated into something like this:

![Junit Eclipse runner](http://i54.tinypic.com/rswjrl.jpg)

* * *

Quite simple, it's a custsom Java Junit Runner that allows you to embed Javascript Unit tests (using Jasmine) in your Java-based projects. It fully integrates with your most beloved IDE, your most hated version control system and of course your most needed CI env. 

So let's rephrase:

* Run Javascript (the <a href="https://github.com/pivotal/jasmine" target="_blank">Jasmine</a> - behavior driven - way) "specs" in Java
* Talks like a duck-erhm, any other Junit Java test. Just use a custom annotation (see below)
* Executes super-fast. No browser required. Hocus-pocus. (Rhino + Envjs magic)

## Does this thing support ...

### Generation of Junit XML Results?

Yes and no. Not explicitly using the Jasmine Junit XML Reporter, but since it's a Java Junit Result, your build process will do that for you. 
Maven surefire plugins will generate the needed result files, for Jenkins to pick up. Your stacktrace/failure message will be something like:

> Expected x to be y (zz.js, #458)

Just like the default Jasmine HTML reporter.
(So, to answer the question: yes!)

### GUI Testing with Envjs? 

Yes! It allows you to test your jQuery plugins or your spaghetti GUI+Logic code, neatly woven together.
You can use <a href="https://github.com/velesin/jasmine-jquery" target="_blank">jasmine-jquery</a> matchers. I've modified `jasmine.Fixtures` to support Envjs+Rhino. This means you can test stuff like this:

```javascript
beforeEach(function() {
  loadFixtures("myFixture.html");
});

it("should be visible and blue", function() {
  var div = $('#myDivInFixtureHtml');
  expect(div).toBeVisible();
  expect(div.css('color')).toBe('blue');
});
```

Fixtures are automatically cleaned up. See src/test/javascript/lib/jasminedir/jasmine-jquery-rhino.js

#### But wait, CSS Style Parsing does not work in Envjs 1.2, how come this does?

See env.utils.js. Cover your eyes - hacks present. 

### Debugging 'n stuff?

Yes! When the debug mode flag in `@JasmineSuite` has been set to `true`, you can use the <a href="http://www.mozilla.org/rhino/debugger.html" target="_blank">Rhino Debugger</a> to set breakpoints.
After pressing "GO", the tests will run and you can inspect stuff and step through the code.

#### What about integrated debugging inside my IDE?

Tough luck. I've tried to get <a href="http://wiki.eclipse.org/JSDT" target="_blank">JSDT</a> working but no avail. 
You can still use Firebug to debug when generating a specRunner HTML file (see below).

## Excellent! What Do I need to do? 

1. Fork this project. 
2. Create some Jasmine specs, place them in some folder.
3. Create a Junit test class, annotate it with `@RunWith(JasmineTestRunner.class)`
4. Fill in the blanks using `@JasmineSuite`

## More options

`@JasmineSuite` allows you to set these options:

* debug: use the built-in Rhino debugger (gives you the chance to set a breakpoint before firing the test suite)
* jsRootDir: the javascript install root dir. Jasmine and other should be installed here (see source)
* sourcesRootDir: your production JS files root dir.
* specs: one or more spec file to run. Default behavior: use java Class name (replaces Test with Spec, see example)
* sources: one or more JS production file which your spec needs (included before specs, d'uh)
* generateSpecRunner: (the HTML output, useful for firefox/firebug debugging etc)

## Requirements

Currently, Jasmine Junit Runner relies on Rhino 1.7R2 (+ es5-shim) & Envjs 1.2 to interpret JS code. It also uses Jamsine 1.0.2 to read your spec files. All js libs are located in test/javascript/lib .

### Dependencies Overview

See the `pom.xml` (Maven2) - you can build the whole thing using:

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

### Using Junit's `@Before` and `@After_`

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

* You can define n number of _PUBLIC_ methods annotated with `@Before` or `@After`
* You can, but don't have to, take the `RhinoContext` object as the only parameter. This allows you to set stuff up in JS space before running the spec.

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
