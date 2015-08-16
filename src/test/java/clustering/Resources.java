package clustering;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Resources {
	public static final String testfilePath = "src\\test\\resources\\test_data.txt";
	public static final File testfile = new File(testfilePath);
	
	private static final List<String> full_expected = new ArrayList<>();
	static {
		full_expected.add("Мама мыла раму.");
		full_expected.add("Пёс Шарик пошёл в лес и пропал.");
		full_expected.add("Жил у нас в доме большой, толстый кот Иваныч. Жирный, неповоротливый. "
				+ "Иваныч любит улечься так, чтобы ему было и тепло, и мягко.");
		full_expected.add("29 ноября 1989;");
	}
	
	public static List<String> getExpected() {
		return full_expected;
	}
}
