package clustering.preparing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//TODO: поменять все System.out и System.err на логи (slf4j)
public class DataReader {
	private static boolean check(File source) {
		return source.exists() && source.canRead();
	}
	
	public static List<String> readData(String filePath) {
		return readData(new File(filePath));
	}
	
	public static List<String> readData(File source) {
		if (!check(source)) {
			System.err.println(" The source file has not been founded or is not readable! ");
			return null;
		}
		
		ArrayList<String> data = new ArrayList<>();
		try (BufferedReader in = new BufferedReader(new FileReader(source))) {
			while (in.ready()) {
				data.add(in.readLine());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
	
	public static List<String> readData(String filePath, int amount) {
		return readData(new File(filePath), amount);
	}
	
	public static List<String> readData(File source, int amount) {
		if (!check(source)) {
			return null;
		}
		
		ArrayList<String> data = new ArrayList<>();
		try (BufferedReader in = new BufferedReader(new FileReader(source))) {
			while (in.ready() && amount > 0) {
				data.add(in.readLine());
				amount--;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
}