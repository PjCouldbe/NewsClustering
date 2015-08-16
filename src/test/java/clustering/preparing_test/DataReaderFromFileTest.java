package clustering.preparing_test;

import static clustering.Resources.testfile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.util.List;

import org.junit.Test;

import clustering.Resources;
import clustering.preparing.DataReader;

public class DataReaderFromFileTest {
	@Test
	public void testReadData() {
		List<String> expected = Resources.getExpected();
		List<String> res = DataReader.readData(testfile);
		assertEquals(res, expected);
	}
	
	@Test
	public void testReadDataFromUnexisted()  {
		List<String> res = DataReader.readData(new File("unexisted.txt"));
		assertNull(res);
	}
	
	@Test (expected = NullPointerException.class)
	public void testReadDataFromNull() {
		DataReader.readData((File)null);
	}
	
	@Test
	public void testReadDataWithAmount() {
		final int amount = 2;
		List<String> res = DataReader.readData(testfile, amount);
		List<String> expected = Resources.getExpected().subList(0, amount);
		
		assertEquals(res, expected);
	}
}