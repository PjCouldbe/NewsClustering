package clustering.preparing_test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import clustering.Resources;
import clustering.preparing.DataCleaner;

@RunWith( JUnit4.class )
public class DataCleanerTest {
	private static String monthsAndDays = "\\d\\d "
			+ "((январ|феврал|апрел|ма)я|(мар|авгус)та|ию[нл]я|(сентя|октя|ноя|дека)бря)";
	private static String externalMask = "^"
					+ monthsAndDays
					+ " \\d?\\d?\\d\\d,? "
					+ "(\\d\\d:\\d\\d)?"
					+ ".*"
					+ "$";
	private static String internalMask = "[^.] [A-ZА-Я]\\w+ ";
	private static List<String> expected;
	
	@BeforeClass
	public void setUp() {
		expected = Resources.getExpected();
	}
	
	@Test
	public void testCleanData() {
		//подготавлиаем данные для этого теста
		for (int i = 0; i < expected.size(); i++) {
			if (expected.get(i).matches(externalMask)) {
				String s = expected.get(i);
				s = expected.get(i).replaceAll(internalMask, ".");
			} else {
				expected.remove(i);
			}
		}
		
		List<String> res = DataCleaner.cleanData(Resources.getExpected(), externalMask, internalMask, ".");
		
		assertEquals(res, expected);
	}
	
	@Test
	public void testCleanWithExternalMask() {
		//подготавлиаем данные для этого теста
		for (int i = 0; i < expected.size(); i++) {
			if (!expected.get(i).matches(externalMask)) {
				expected.remove(i);
			} 
		}
		
		List<String> res = DataCleaner.cleanData(Resources.getExpected(), externalMask, null, ".");
		
		assertEquals(res, expected);
	}
	
	@Test
	public void testCleanWithInternalMask() {
		//подготавлиаем данные для этого теста
		for (String s : expected) {
			s = s.replaceAll(internalMask, ".");
		}
		
		List<String> res = DataCleaner.cleanData(Resources.getExpected(), null, internalMask, ".");
		
		assertEquals(res, expected);
	}
	
	@Test
	//если на replacement передаётся null, то он заменяется на пустую строку
	public void testCleanWithNullReplacement() {   
		//подготавлиаем данные для этого теста
		for (String s : expected) {
			s = s.replaceAll(internalMask, "");
		}
		
		List<String> res = DataCleaner.cleanData(Resources.getExpected(), null, internalMask, null);
		
		assertEquals(res, expected);
	}
}
