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
	int 	num 		= 0;
	
	public double getAveBandWidth() {
		return bandWidth / num;
	}
	
	public double getAveBuildTime() {
		return buildTime / num;
	}
	
	
	public void reSet() {
		bandWidth 	= 0;
		buildTime 	= 0;
		num 		= 0;
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
		StringBuffer sb = new StringBuffer();
		sb.append("bandWidth: " + getAveBandWidth() + " KB\n");
		sb.append("buildTime: " + getAveBuildTime() + " ms\n");
		return sb.toString();
	}

}
