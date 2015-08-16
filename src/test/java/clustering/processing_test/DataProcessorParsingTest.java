package clustering.processing_test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DataProcessorParsingTest extends Assert {
	@Before
	public void setUp() {
		
	}
	
	@Test
	public void testParse() {
		//TODO: Default parse test on normal predefined data
	}
	
	@Test
	public void testParseForEmptyParseResult() {
		//TODO: testing parse on data, which will give the null or empty parse-result
	}
	
	@Test (expected = NullPointerException.class)
	public void testParseWithNull() {
		//TODO: testing parse on null data
	}
}
