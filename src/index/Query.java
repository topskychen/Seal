/**
 * 
 */
package index;

import spatialindex.IShape;


/**
 * @author chenqian
 *
 */
public class Query {

	public static enum QUERY_TYPE {range_query, knn, skyline};
	
	private QUERY_TYPE	type;
	private IShape		shape;
	
	/**
	 * 
	 */
	public Query(QUERY_TYPE _type, IShape _shape) {
		type = _type;
		shape = _shape;
		
	}
	
	public QUERY_TYPE getType() {
		return type;
	}
	
	public IShape getShape() {
		return shape;
	}

}
