/**
 * 
 */
package utility;

/**
 * @author chenqian
 * 
 */
public class StatisticsQuery {

	double	prepareTime	= 0;
	double	verifyTime	= 0;
	double	voSize		= 0;
	double 	entryNum 	= 0;
	double	resultNum	= 0;
	int		num			= 0;

	public void append(double prepareTime, double verifyTime, long voSize, long resultNum, long entryNum) {
		this.prepareTime += prepareTime;
		this.verifyTime += verifyTime;
		this.voSize += voSize;
		this.resultNum += resultNum;
		this.entryNum += entryNum;
		num++;
	}

	public double getAvePrepareTime() {
		return prepareTime / num;
	}

	public double getAveVerifyTime() {
		return verifyTime / num;
	}

	public double getAveVOSize() {
		return voSize / num;
	}
	
	public double getAveResultNum() {
		return resultNum / num;
	}
	
	public double getAveEntryNum() {
		return entryNum / num;
	}

	public void reSet() {
		prepareTime = 0;
		verifyTime = 0;
		voSize = 0;
		resultNum = 0;
		entryNum = 0;
		num = 0;
	}

	/**
	 * 
	 */
	public StatisticsQuery() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public String toString() {
		StringBuffer sb = new StringBuffer("---------StatQ--------\n");
		sb.append("prepareTime: " + getAvePrepareTime() + " ms\n");
		sb.append("verifyTime: " + getAveVerifyTime() + " ms\n");
		sb.append("VO Size: " + getAveVOSize() / 1024 + " KB\n");
		sb.append("resultNum: " + getAveResultNum() + "\n");
		sb.append("EntryNum: " + getAveEntryNum() + "\n");
		sb.append("-------------------\n");
		return sb.toString();
	}

}
