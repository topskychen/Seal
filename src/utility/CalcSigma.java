/**
 * 
 */
package utility;

import index.SearchIndex.INDEX_TYPE;

import java.util.ArrayList;

import party.DataOwner;
import party.TrustedRegister;
import spatialindex.Point;
import utility.EncFun.ENC_TYPE;
import crypto.AES;

/**
 * @author chenqian 5.377038124319929
 */
public class CalcSigma {
	String	fileName	= Global.TEST_FILE_DIR + "/TDrive";
	int		start		= 0;
	int		len			= 250;

	/**
	 * 
	 */
	public CalcSigma() {
		TrustedRegister.sk = AES.getSampleKey();
		TrustedRegister.specifyEncFun(ENC_TYPE.OTPad, fileName);
		ArrayList<DataOwner> dataOwners = new ArrayList<DataOwner>();
		DataOwner.initData(dataOwners, fileName, INDEX_TYPE.QTree, start, len);
		double sigma2 = 0;
		int num = 0;
		for (DataOwner owner : dataOwners) {
			ArrayList<Point> points = new ArrayList<>();
			for (int i = start; i < start + len; i++) {
				Point point = (Point) owner.getPoint(i);
				if (point != null) {
					points.add(point);
				}
			}
			for (int i = 1; i < points.size(); i++) {
				Point a = points.get(i - 1), b = points.get(i);
				sigma2 = Math.max(
						(b.getCoord(0) - a.getCoord(0))
								* (b.getCoord(0) - a.getCoord(0)),
						(b.getCoord(1) - a.getCoord(1))
								* (b.getCoord(1) - a.getCoord(1)));
				num++;
			}
		}
		double sigma = Math.sqrt(sigma2 / num);
		System.out.println(sigma);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CalcSigma calc = new CalcSigma();
	}

}
