/**
 * 
 */
package utility;

/**
 * @author chenqian
 * 
 */
public class StatisticsIndex {

	double	buildTime	= 0;
	double	indexSize	= 0;
	int		num			= 0;

	public void append(double buildTime, double indexSize) {
		this.buildTime += buildTime;
		this.indexSize += indexSize;
		this.num++;
	}

	public double getAveBuildTime() {
		return buildTime / num;
	}

	public double getAveIndexSize() {
		return indexSize / num;
	}

	public void reSet() {
		buildTime = 0;
		indexSize = 0;
		num = 0;
	}

	/**
	 * 
	 */
	public StatisticsIndex() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	public String toString() {
		StringBuffer sb = new StringBuffer("---------Index Costs------\n");
		sb.append("buildTime : " + getAveBuildTime() + " ms\n");
		sb.append("indexSize : " + getAveIndexSize() / 1024 / 1024 + " MB\n");
		sb.append("--------------------\n");
		return sb.toString();
	}

}
