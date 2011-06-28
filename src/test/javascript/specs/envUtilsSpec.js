
describe("envjs fixes", function() {

	describe("CSS2 style property support", function() {

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
	
	describe("window setTimeout", function() {
	
		it("should wait one second before executing", function() {
			var done = false;
			window.setTimeout(function() {
				done = true;
			}, 1000);
		
			waitsFor(function() {
				return done === true;
			});
		
			runs(function() {
				expect(done).toBeTruthy();
			});
		});
	
	});

});