/**
 * GetLegendaEventiLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package iseries.wsbeans.getlegendaeventi;

import javax.xml.namespace.QName;

@SuppressWarnings("rawtypes")
public class GetLegendaEventiLocator extends org.apache.axis.client.Service implements GetLegendaEventi {

	private static final long serialVersionUID = 1L;

	public GetLegendaEventiLocator() {
	}

	public GetLegendaEventiLocator(org.apache.axis.EngineConfiguration config) {
		super(config);
	}

	public GetLegendaEventiLocator(String wsdlLoc, QName sName) throws javax.xml.rpc.ServiceException {
		super(wsdlLoc, sName);
	}

	// Use to get a proxy class for GetLegendaEventiServicesPort
	private String GetLegendaEventiServicesPort_address = "http://wsr.brt.it:10041/web/GetLegendaEventiService/GetLegendaEventi";

	public String getGetLegendaEventiServicesPortAddress() {
		return GetLegendaEventiServicesPort_address;
	}

	// The WSDD service name defaults to the port name.
	private String GetLegendaEventiServicesPortWSDDServiceName = "GetLegendaEventiServicesPort";

	public String getGetLegendaEventiServicesPortWSDDServiceName() {
		return GetLegendaEventiServicesPortWSDDServiceName;
	}

	public void setGetLegendaEventiServicesPortWSDDServiceName(String name) {
		GetLegendaEventiServicesPortWSDDServiceName = name;
	}

	public iseries.wsbeans.getlegendaeventi.GetLegendaEventiServices getGetLegendaEventiServicesPort()
			throws javax.xml.rpc.ServiceException {
		java.net.URL endpoint;
		try {
			endpoint = new java.net.URL(GetLegendaEventiServicesPort_address);
		} catch (java.net.MalformedURLException e) {
			throw new javax.xml.rpc.ServiceException(e);
		}
		return getGetLegendaEventiServicesPort(endpoint);
	}

	public iseries.wsbeans.getlegendaeventi.GetLegendaEventiServices getGetLegendaEventiServicesPort(
			java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
		try {
			iseries.wsbeans.getlegendaeventi.GetLegendaEventiServicesPortBindingStub _stub = new iseries.wsbeans.getlegendaeventi.GetLegendaEventiServicesPortBindingStub(
					portAddress, this);
			_stub.setPortName(getGetLegendaEventiServicesPortWSDDServiceName());
			return _stub;
		} catch (org.apache.axis.AxisFault e) {
			return null;
		}
	}

	public void setGetLegendaEventiServicesPortEndpointAddress(String address) {
		GetLegendaEventiServicesPort_address = address;
	}

	/**
	 * For the given interface, get the stub implementation. If this service has
	 * no port for the given interface, then ServiceException is thrown.
	 */
	public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
		try {
			if (iseries.wsbeans.getlegendaeventi.GetLegendaEventiServices.class
					.isAssignableFrom(serviceEndpointInterface)) {
				iseries.wsbeans.getlegendaeventi.GetLegendaEventiServicesPortBindingStub _stub = new iseries.wsbeans.getlegendaeventi.GetLegendaEventiServicesPortBindingStub(
						new java.net.URL(GetLegendaEventiServicesPort_address), this);
				_stub.setPortName(getGetLegendaEventiServicesPortWSDDServiceName());
				return _stub;
			}
		} catch (Throwable t) {
			throw new javax.xml.rpc.ServiceException(t);
		}
		throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  "
				+ (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
	}

	/**
	 * For the given interface, get the stub implementation. If this service has
	 * no port for the given interface, then ServiceException is thrown.
	 */
	public java.rmi.Remote getPort(QName portName, Class serviceEndpointInterface)
			throws javax.xml.rpc.ServiceException {
		if (portName == null) {
			return getPort(serviceEndpointInterface);
		}
		String inputPortName = portName.getLocalPart();
		if ("GetLegendaEventiServicesPort".equals(inputPortName)) {
			return getGetLegendaEventiServicesPort();
		} else {
			java.rmi.Remote _stub = getPort(serviceEndpointInterface);
			((org.apache.axis.client.Stub) _stub).setPortName(portName);
			return _stub;
		}
	}

	public QName getServiceName() {
		return new QName("http://getlegendaeventi.wsbeans.iseries", "GetLegendaEventi");
	}

	private java.util.HashSet<QName> ports = null;

	public java.util.Iterator getPorts() {
		if (ports == null) {
			ports = new java.util.HashSet<QName>();
			ports.add(new QName("http://getlegendaeventi.wsbeans.iseries", "GetLegendaEventiServicesPort"));
		}
		return ports.iterator();
	}

	/**
	 * Set the endpoint address for the specified port name.
	 */
	public void setEndpointAddress(String portName, String address) throws javax.xml.rpc.ServiceException {

		if ("GetLegendaEventiServicesPort".equals(portName)) {
			setGetLegendaEventiServicesPortEndpointAddress(address);
		} else { // Unknown Port Name
			throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
		}
	}

	/**
	 * Set the endpoint address for the specified port name.
	 */
	public void setEndpointAddress(QName portName, String address) throws javax.xml.rpc.ServiceException {
		setEndpointAddress(portName.getLocalPart(), address);
	}

}
