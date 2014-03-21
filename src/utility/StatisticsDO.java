/**
 * 
 */
package utility;

/**
 * @author chenqian
 * 
 */
public class StatisticsDO {

	int		num			= 0;
	double	buildTime	= 0;
	double	entrySize	= 0;

	public void append(double buildTime, double entrySize) {
		this.buildTime += buildTime;
		this.entrySize += entrySize;
		num++;
	}

	public double getAveBuildTime() {
		return buildTime / num;
	}

	public double getAveEntrySize() {
		return entrySize / num;
	}

	public void reSet() {
		num = 0;
		buildTime = 0;
		entrySize = 0;
	}

	/**
	 * 
	 */
	public StatisticsDO() {
		reSet();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	public String toString() {
		StringBuffer sb = new StringBuffer("---------DO Costs------\n");
		sb.append("buildTime : " + getAveBuildTime() + " us\n");
		sb.append("sealSize : " + getAveEntrySize() + " B\n");
		sb.append("----------------\n");
		return sb.toString();
	}
}
