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
public class ODQGenerator {

	Random random = new Random();
	
	public ODQGenerator(int size, String fileName) {
		File file = new File(fileName + ".pl");
		PrintWriter pw;
		try {
			pw = new PrintWriter(file);
			int[] data = new int[size];
			for (int i = 0; i < size; i ++) {
				data[i] = Math.abs(random.nextInt(1 << 30));
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
	public ODQGenerator() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
