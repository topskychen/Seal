/**
 * 
 */
package generator;

import index.SearchIndex.INDEX_TYPE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import party.DataOwner;
import party.TrustedRegister;
import spatialindex.IShape;
import utility.EncFun.ENC_TYPE;
import utility.Global;
import crypto.AES;

/**
 * @author chenqian
 *
 */
public class MDGenerator {
	Random random = new Random();

	int[] genRange(int b0, int b1,  double ratio) {
		int bound = b1 - b0;
		int l = random.nextInt((int) (bound * (1 - ratio)));
		return new int[] {l, l + (int) (bound * ratio)};
	}
	
	/**
	 * 
	 */
	public MDGenerator(int dim) {
		
		int start = 0, end = 50;
		String fileName = Global.TEST_FILE_DIR + "/TDrive";
		TrustedRegister.sk = AES.getSampleKey();
		TrustedRegister.specifyEncFun(ENC_TYPE.OTPad, fileName);
		ArrayList<DataOwner> owners = new ArrayList<DataOwner>();
		DataOwner.initData(owners, fileName, INDEX_TYPE.QTree, start, end - start + 1); 
		
		/**
		 * data
		 */
		try {
			PrintWriter pw = new PrintWriter(new File(fileName + dim + ".pl"));
			for (DataOwner owner : owners) {
				StringBuilder sb = new StringBuilder();
				sb.append(owner.getId() + "\n");
				for (int i = start; i < end; ++i) {
					IShape shape = owner.getPoint(i);
					if (shape == null) {
						System.out.println(owner.getId() + " : " + i);
						continue;
					}
					for (int j = 0; j < shape.getDimension(); j++) {
						sb.append((int) shape.getMBR().m_pLow[j] + " ");
					}
					for (int j = 0; j < dim - shape.getDimension(); j++) {
						sb.append(random.nextInt(300) + " ");
					}
					sb.append(i + "\t");
				}
				String line = sb.toString().trim();
				pw.println(line);
			}
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("data fin!");
		/**
		 * query
		 */
		try {
			PrintWriter pw = new PrintWriter(new File(fileName + dim + "_0.02.qr"));
			double ratio = Math.pow(0.02, 1.0/dim);
			for (int i = 0; i < 1000; ++i) {
				int[] low = new int[dim];
				int[] high = new int[dim];
				int[] range = genRange(0, (int) Global.BOUND, ratio);
				low[0] = range[0];
				high[0] = range[1];
				range = genRange(0, (int) Global.BOUND, ratio);
				low[1] = range[0];
				high[1] = range[1];
				for (int j = 0; j < dim-2; ++j) {
					range = genRange(0, 300, ratio);
					low[2+j] = range[0];
					high[2+j] = range[1];
				}
				String line = "";
				for (int j = 0; j < dim; ++j) {
					line += low[j] + " ";
				}
				for (int j = 0; j < dim; ++j) {
					line += high[j] + " ";
				}
				line.trim();
				pw.println(line);
			}
			pw.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("query fin!");
	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MDGenerator gen = new MDGenerator(2);
	}

}
