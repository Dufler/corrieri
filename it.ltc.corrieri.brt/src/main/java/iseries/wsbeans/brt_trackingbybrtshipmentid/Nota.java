/**
 * Nota.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package iseries.wsbeans.brt_trackingbybrtshipmentid;

public class Nota implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private java.lang.String DESCRIZIONE;

	public Nota() {
	}

	public Nota(java.lang.String DESCRIZIONE) {
		this.DESCRIZIONE = DESCRIZIONE;
	}

	/**
	 * Gets the DESCRIZIONE value for this Nota.
	 * 
	 * @return DESCRIZIONE
	 */
	public java.lang.String getDESCRIZIONE() {
		return DESCRIZIONE;
	}

	/**
	 * Sets the DESCRIZIONE value for this Nota.
	 * 
	 * @param DESCRIZIONE
	 */
	public void setDESCRIZIONE(java.lang.String DESCRIZIONE) {
		this.DESCRIZIONE = DESCRIZIONE;
	}

	private java.lang.Object __equalsCalc = null;

	public synchronized boolean equals(java.lang.Object obj) {
		if (!(obj instanceof Nota))
			return false;
		Nota other = (Nota) obj;
		// if (obj == null) return false;
		if (this == obj)
			return true;
		if (__equalsCalc != null) {
			return (__equalsCalc == obj);
		}
		__equalsCalc = obj;
		boolean _equals;
		_equals = true && ((this.DESCRIZIONE == null && other.getDESCRIZIONE() == null)
				|| (this.DESCRIZIONE != null && this.DESCRIZIONE.equals(other.getDESCRIZIONE())));
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
		__hashCodeCalc = false;
		return _hashCode;
	}

	// Type metadata
	private static org.apache.axis.description.TypeDesc typeDesc = new org.apache.axis.description.TypeDesc(Nota.class,
			true);

	static {
		typeDesc.setXmlType(
				new javax.xml.namespace.QName("http://brt_trackingbybrtshipmentid.wsbeans.iseries/", "nota"));
		org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
		elemField.setFieldName("DESCRIZIONE");
		elemField.setXmlName(new javax.xml.namespace.QName("", "DESCRIZIONE"));
		elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
	@SuppressWarnings("rawtypes")
	public static org.apache.axis.encoding.Serializer getSerializer(java.lang.String mechType,
			java.lang.Class _javaType, javax.xml.namespace.QName _xmlType) {
		return new org.apache.axis.encoding.ser.BeanSerializer(_javaType, _xmlType, typeDesc);
	}

	/**
	 * Get Custom Deserializer
	 */
	@SuppressWarnings("rawtypes")
	public static org.apache.axis.encoding.Deserializer getDeserializer(java.lang.String mechType,
			java.lang.Class _javaType, javax.xml.namespace.QName _xmlType) {
		return new org.apache.axis.encoding.ser.BeanDeserializer(_javaType, _xmlType, typeDesc);
	}

}
