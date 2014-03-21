/**
 * 
 */
package utility;

/**
 * @author chenqian
 * 
 */
public class StatisticsUpdate {

	double	bandWidth	= 0;
	double	buildTime	= 0;
	int		planNum		= 0;
	int		realNum		= 0;
	int		rounds		= 0;

	public double getAveBandWidth() {
		return bandWidth / rounds;
	}

	public double getAveBuildTime() {
		return buildTime / rounds;
	}

	public void appendNum(int planNum, int realNum) {
		this.planNum += planNum;
		this.realNum += realNum;
	}

	public void reSet() {
		bandWidth = 0;
		buildTime = 0;
		planNum = 0;
		realNum = 0;
		rounds = 0;
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
		if (rounds == 0 || planNum == 0)
			sb.append("real : " + realNum + ", plan: " + planNum);
		else
			sb.append("real / plan: " + "(" + realNum / rounds + "/" + planNum
					/ rounds + ") = " + 100.0 * realNum / planNum + " %\n");
		sb.append("------------------\n");
		return sb.toString();
	}

	public void append(double buildTime, int bandWidth) {
		this.bandWidth += bandWidth;
		this.buildTime += buildTime;
		this.rounds++;
	}

}
