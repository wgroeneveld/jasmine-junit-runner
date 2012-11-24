
describe("envjs fixes", function() {

	describe("Envjs event handling fixes", function() {
		beforeEach(function() {
			loadFixtures("formevents.html");
		});
		
		describe("focussing events", function() {
			it("should set activeElement when focussing an input element", function() {
				$("#input").focus();
				expect(document.activeElement.id).toBe("input");
				expect($(":focus")).toBe("#input");
			});
			
			it("should set activeElement when focussing a textarea element", function() {
				$("#area").focus();
				expect(document.activeElement.id).toBe("area");
				expect($(":focus")).toBe("#area");
			});
		});
		
		describe("form submit events", function() {
			it("should be able to catch a formsubmit event", function() {
				var submitted = false;
				$("#form").submit(function() {
					submitted = true;
				});
				
				$("#form").submit();
				waitsFor(function() {
					return submitted === true;
				});
				
				runs(function() {
					expect(submitted).toBeTruthy();
				});
			});
		});
		
		describe("Checkbox click events", function() {
			it("should set the state of the checkbox to checked if not checked when clicked", function() {
				$("#checkbox").click();
				expect($("#checkbox")).toBeChecked();
			});
			
			it("should set the state of the checkbox to unchecked if checked when clicked", function() {
				$("#checkbox").attr('checked', true);
				$("#checkbox").click();
				expect($("#checkbox")).not.toBeChecked();
			});
			
			it("should still fire the click event after clicking on a checkbox", function() {
				var clicked = false;
				$("#checkbox").click(function() {
					clicked = true;
				});
				
				waitsFor(function() {
					return clicked;
				});
				$("#checkbox").click();
				runs(function() {
					expect(clicked).toBeTruthy();
				});
			});
		});
	});
	
	describe("CSS2 style property support for parsing style attributes", function() {
		beforeEach(function() {
			loadFixtures("styleAttributes.html");
		});
			
		it("should get a style attribute from a static DOM element", function() {
			var div = document.getElementById("div");
			expect(div.style.color).toBe("blue");
		});
		
		it("should get a style attribute with dashes using camelCasing properties", function() {
			var spanStyle = document.getElementById("span").style;
			
			expect(spanStyle.backgroundColor).toBe("green");
			expect(spanStyle.fontSize).toBe("8pt");
			expect(spanStyle.fontFamily).toBe("verdana");
		});
	});
	
	describe("CSS2 style property support for setting values", function() {

		var someColor = "#FFFFFF";
		var someFont = "12px 'Bitstream Vera Sans Mono','Courier',monospace";
	
		it("should be visible and displayed by default for all new elements", function() {
			var elStyle = document.createElement("b").style;
		
			expect(elStyle.display).toBeFalsy();
			expect(elStyle.visibility).toBeFalsy();
		});
	
		it("should be able to set a style value through setters", function() {
			var someB = document.createElement("b");
			someB.style.color = someColor;
			
			expect(someB.style.color).toBe(someColor);
		});
		
		it("should have unique style values per DOM element", function() {
			var someEl1 = document.createElement("b");
			var someEl2 = document.createElement("b");
			
			someEl1.style.color = someColor;
			someEl2.style.font = someFont;
			
			expect(someEl1.style.font).toBeFalsy();
			expect(someEl2.style.color).toBeFalsy();
		});
	
	});
	
	describe("timer based events", function() {
	
		describe("setTimeout", function() {
			it("should wait one second before executing", function() {
				var done = false;
				window.setTimeout(function() {
					done = true;
				}, 50);
				
				waitsFor(function() {
					return done === true;
				});
				
				runs(function() {
					expect(done).toBeTruthy();
				});
			});
			
			it("should return a unique timerID when the timeout has been set which can be cancelled", function() {
				var done = false;
				var timerID = window.setTimeout(function() {
					done = true;
				}, 10);
				var timerID2 = window.setTimeout(function() { }, 10);

				window.clearTimeout(timerID);
				waits(50);
				
				runs(function() {
					expect(typeof(timerID)).toEqual("number");
					expect(timerID).not.toEqual(timerID2);
					expect(done).toBeFalsy();
				});
			});
			
			it("should be able to use clearInterval for timeouts", function() {
				var done = false;
				var timerID = window.setTimeout(function() {
					done = true;
				}, 10);

				window.clearInterval(timerID);
				waits(50);
				
				runs(function() {
					expect(done).toBeFalsy();
				});
			});
		});
		
		describe("setInterval", function() {
			it("should call the callback method x times until the interval has been stopped", function() {
				var count = 0, storedCount;
				var intervalId = window.setInterval(function() {
					count++;
				}, 20);
				
				waitsFor(function() {
					return count > 3;
				});
				
				runs(function() {
					storedCount = count;
					window.clearInterval(intervalId);
				});
				waits(100);
				
				runs(function() {
					expect(storedCount).toEqual(count);
				});
			});
			
			it("should be able to use setTimeout and setInterval which create unique return IDs", function() {
				var id1 = window.setTimeout(function() {}, 10);
				var id2 = window.setInterval(function() {}, 10);
				
				waits(50);
				this.after(function() {
					window.clearInterval(id2);
				});
				
				runs(function() {
					expect(id1 < id2).toBeTruthy();
				});
			});
			
			it("should be able to use clearTimeout for intervals", function() {
				var count = 0, storedCount;
				var intervalId = window.setInterval(function() {
					count++;
				}, 10);
				
				waitsFor(function() {
					return count > 1;
				});
				
				runs(function() {
					storedCount = count;
					window.clearTimeout(intervalId);
				});
				waits(100);
				
				runs(function() {
					expect(storedCount).toEqual(count);
				});
			});
		});
	
	});

});