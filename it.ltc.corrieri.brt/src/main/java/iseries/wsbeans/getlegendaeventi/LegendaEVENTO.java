/**
 * LegendaEVENTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package iseries.wsbeans.getlegendaeventi;

import javax.xml.namespace.QName;

@SuppressWarnings("rawtypes")
public class LegendaEVENTO implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private String DESCRIZIONE;

	private String ID;

	public LegendaEVENTO() {
	}

	public LegendaEVENTO(String DESCRIZIONE, String ID) {
		this.DESCRIZIONE = DESCRIZIONE;
		this.ID = ID;
	}

	/**
	 * Gets the DESCRIZIONE value for this LegendaEVENTO.
	 * 
	 * @return DESCRIZIONE
	 */
	public String getDESCRIZIONE() {
		return DESCRIZIONE;
	}

	/**
	 * Sets the DESCRIZIONE value for this LegendaEVENTO.
	 * 
	 * @param DESCRIZIONE
	 */
	public void setDESCRIZIONE(String DESCRIZIONE) {
		this.DESCRIZIONE = DESCRIZIONE;
	}

	/**
	 * Gets the ID value for this LegendaEVENTO.
	 * 
	 * @return ID
	 */
	public String getID() {
		return ID;
	}

	/**
	 * Sets the ID value for this LegendaEVENTO.
	 * 
	 * @param ID
	 */
	public void setID(String ID) {
		this.ID = ID;
	}

	private Object __equalsCalc = null;

	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof LegendaEVENTO))
			return false;
		LegendaEVENTO other = (LegendaEVENTO) obj;
		// if (obj == null) return false;
		if (this == obj)
			return true;
		if (__equalsCalc != null) {
			return (__equalsCalc == obj);
		}
		__equalsCalc = obj;
		boolean _equals;
		_equals = true
				&& ((this.DESCRIZIONE == null && other.getDESCRIZIONE() == null)
						|| (this.DESCRIZIONE != null && this.DESCRIZIONE.equals(other.getDESCRIZIONE())))
				&& ((this.ID == null && other.getID() == null) || (this.ID != null && this.ID.equals(other.getID())));
		__equalsCalc = null;
		return _equals;
	}

	private boolean __hashCodeCalc = false;

	public synchronized int hashCode() {
		if (__hashCodeCalc) {
			return 0;
		}
		__hashCodeCalc = true;
		int _hashCode = 1;
		if (getDESCRIZIONE() != null) {
			_hashCode += getDESCRIZIONE().hashCode();
		}
		if (getID() != null) {
			_hashCode += getID().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

	// Type metadata
	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(
			LegendaEVENTO.class, true);

	static {
		typeDesc.setXmlType(new QName("http://getlegendaeventi.wsbeans.iseries", "legendaEVENTO"));
		org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("DESCRIZIONE");
		elemField.setXmlName(new QName("", "DESCRIZIONE"));
		elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
		elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("ID");
		elemField.setXmlName(new QName("", "ID"));
		elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
		elemField.setNillable(false);
		typeDesc.addFieldDesc(elemField);
	}

	/**
	 * Return type metadata object
	 */
	public static org.apache.axis.description.TypeDesc getTypeDesc() {
		return typeDesc;
	}

	/**
	 * Get Custom Serializer
	 */
	public static org.apache.axis.encoding.Serializer getSerializer(String mechType, Class _javaType, QName _xmlType) {
		return new org.apache.axis.encoding.ser.BeanSerializer(_javaType, _xmlType, typeDesc);
	}

	/**
	 * Get Custom Deserializer
	 */
	public static org.apache.axis.encoding.Deserializer getDeserializer(String mechType, Class _javaType,
			QName _xmlType) {
		return new org.apache.axis.encoding.ser.BeanDeserializer(_javaType, _xmlType, typeDesc);
	}

}
