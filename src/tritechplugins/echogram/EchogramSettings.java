package tritechplugins.echogram;

import java.io.Serializable;

public class EchogramSettings implements Serializable, Cloneable{

	public static final long serialVersionUID = 1L;
	
	public enum ValueType { MEAN, MAX};
	
	private ValueType valueType = ValueType.MAX;
	
	private int nBands = 1;
	
	private int maxOf = 2; // home may of the largest values to make mean of. 

	@Override
	protected EchogramSettings clone() {
		try {
			return (EchogramSettings) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @return the valueType
	 */
	public ValueType getValueType() {
		return valueType;
	}

	/**
	 * @param valueType the valueType to set
	 */
	public void setValueType(ValueType valueType) {
		this.valueType = valueType;
	}

	/**
	 * @return the nBands
	 */
	public int getnBands() {
		return nBands;
	}

	/**
	 * @param nBands the nBands to set
	 */
	public void setnBands(int nBands) {
		this.nBands = nBands;
	}

	/**
	 * @return the maxOf
	 */
	public int getMaxOf() {
		return maxOf;
	}

	/**
	 * @param maxOf the maxOf to set
	 */
	public void setMaxOf(int maxOf) {
		this.maxOf = maxOf;
	}

	

}
