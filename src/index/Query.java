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

	public static enum QueryType {range_query, knn, skyline};
	
	private QueryType	type;
	private IShape		shape;
	private int			k;
	
	/**
	 * 
	 */
	public Query(QueryType _type, IShape _shape) {
		type = _type;
		shape = _shape;
	}
	
	public Query(QueryType _type, IShape _shape, int _k) {
		type = _type;
		shape = _shape;
		k = _k;
	}
	
	public QueryType getType() {
		return type;
	}
	
	public IShape getShape() {
		return shape;
	}
	
	public void setK(int _k) {
		k = _k;
	}
	
	public int getK() {
		return k;
	}

}
