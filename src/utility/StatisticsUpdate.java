/**
 * 
 */
package utility;

/**
 * @author chenqian
 *
 */
public class StatisticsUpdate {

	double 	bandWidth	= 0;
	double 	buildTime 	= 0;
	int 	planNum 	= 0;
	int 	realNum 	= 0;
	
	public double getAveBandWidth() {
		return bandWidth / realNum;
	}
	
	public double getAveBuildTime() {
		return buildTime / realNum;
	}
	
	public void appendBandWidth(double bandWidth) {
		this.bandWidth 	+= bandWidth;
	}
	
	public void appendBuildTime(double buildTime) {
		this.buildTime += buildTime;
	}
	
	public void appendNum(int planNum, int realNum) {
		this.planNum += planNum;
		this.realNum += realNum;
	}
	
	public void reSet() {
		bandWidth 	= 0;
		buildTime 	= 0;
		planNum 	= 0;
		realNum		= 0;
	}
	
	
	/**
	 * 
	 */
	public StatisticsUpdate() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("----------StatU--------\n");
		sb.append("bandWidth: " + getAveBandWidth() / 1000 + " KB\n");
		sb.append("buildTime: " + getAveBuildTime() + " ms\n");
		sb.append("real / plan: " + 100.0 * realNum / planNum + " %");
		sb.append("------------------\n");
		return sb.toString();
	}

}
