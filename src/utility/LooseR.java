/**
 * 
 */
package utility;

import index.Entry;
import index.SearchIndex.INDEX_TYPE;

import java.util.ArrayList;

import party.Client;
import party.DataOwner;
import party.TrustedRegister;
import spatialindex.Point;
import spatialindex.Region;
import timer.Timer;
import utility.EncFun.ENC_TYPE;
import crypto.AES;

/**
 * @author chenqian
 * 
 */
public class LooseR {

	String					fileName	= Sim.fileName;
	int						start		= 0;
	int						len			= 250;
	ArrayList<DataOwner>	dataOwners;
	ArrayList<Region>		queries;

	/**
	 * 
	 */
	public LooseR() {
		TrustedRegister.sk = AES.getSampleKey();
		TrustedRegister.specifyEncFun(ENC_TYPE.OTPad, fileName);
		dataOwners = new ArrayList<DataOwner>();
		DataOwner.initData(dataOwners, fileName, INDEX_TYPE.QTree, start, len);
		queries = Client.initQuery(fileName + "_0.02");
	}

	public void run(double L) {
		int outOfBounds = 0;
		int interQueries = 0;
		int tot = 0;
		for (DataOwner owner : dataOwners) {
			Region region = new Region(new double[] { 0, 0 }, new double[] { 0,
					0 });
			for (int i = start; i < start + len; i++) {
				Point point = (Point) owner.getPoint(i);
				Region query = queries.get(i);
				if (point == null) {
					if (query.intersects(region)) {
						interQueries++;
					}
					continue;
				}
				tot++;
				if (!region.contains(point)) {
					outOfBounds++;
					region = new Region(new double[] { point.getCoord(0) - L,
							point.getCoord(1) - L }, new double[] {
							point.getCoord(0) + L, point.getCoord(1) + L });
				}
				if (query.intersects(region)) {
					interQueries++;
				}
			}
		}
		System.out.println("------------" + L + "--------------");
		Timer timer = new Timer();
		ArrayList<Entry> entries = new ArrayList<Entry>();
		timer.reset();
		for (int i = 0; i < outOfBounds; i++) {
			entries.add(Seal.getSample(i));
		}
		timer.stop();
		double doCPU = timer.timeElapseinMs() / len;
		System.out.println(doCPU + " ms");
		timer.reset();
		for (int i = 0; i < interQueries; i++) {
			entries.get(i).verify();
		}
		timer.stop();
		double vrfCPU = timer.timeElapseinMs() / len;
		System.out.println(vrfCPU + " ms");
		System.out.println(doCPU + vrfCPU + " ms");
		System.out.println(1.0 * outOfBounds / tot);
		System.out.println("----------------------------");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double L = 50, tmp = L;
		LooseR looseR = new LooseR();
		looseR.run(L);
		for (int i = 0; i < 4; i++) {
			tmp *= Math.sqrt(2);
			looseR.run(tmp);
		}
		tmp = L;
		for (int i = 0; i < 4; i++) {
			tmp /= Math.sqrt(2);
			looseR.run(tmp);
		}
		looseR.run(L);
	}
}
