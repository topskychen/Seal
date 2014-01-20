/**
 * 
 */
package generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;

/**
 * @author chenqian
 *
 */
public class ODGenerator {

	Random random = new Random();
	
	public ODGenerator(int size, String fileName) {
		File file = new File(fileName + ".pl");
		if (file.exists()) {
			System.out.println("file " + fileName + ".pl exists, please delete it first.");
			return;
		}
		PrintWriter pw;
		try {
			pw = new PrintWriter(file);
			int[] data = new int[size];
			for (int i = 0; i < size; i ++) {
				data[i] = Math.abs(random.nextInt(1 << 27));
			}
			Arrays.sort(data);
			for (int i = 0; i < size; i ++) {
				pw.println(data[i]);
			}
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Gen fin!");
	}
	
	/**
	 * 
	 */
	public ODGenerator() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length == 2) {
			new ODGenerator(Integer.parseInt(args[0]), args[1]);
		} else if (args.length == 0) {
			new ODGenerator(10, "./data/OD10");
		} else {
			System.out.println("Error!");
		}
	}

}
