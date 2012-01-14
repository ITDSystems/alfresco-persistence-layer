package ru.itdsystems.alfresco.persistence;

import java.util.HashMap;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class OrbeonNamespaceContext implements NamespaceContext {

	private HashMap<String, String> namespaces;

	public OrbeonNamespaceContext() {
		// TODO
		// change these hard-coded lines
		namespaces = new HashMap<String, String>();
		namespaces.put("xxi", "http://orbeon.org/oxf/xml/xinclude");
		namespaces.put("xi", "http://www.w3.org/2001/XInclude");
		namespaces.put("ev", "http://www.w3.org/2001/xml-events");
		namespaces.put("xforms", "http://www.w3.org/2002/xforms");
		namespaces.put("xhtml", "http://www.w3.org/1999/xhtml");
		namespaces.put("saxon", "http://saxon.sf.net/");
		namespaces.put("xs", "http://www.w3.org/2001/XMLSchema");
		namespaces.put("xbl", "http://www.w3.org/ns/xbl");
		namespaces.put("exforms", "http://www.exforms.org/exf/1-0");
		namespaces.put("sql", "http://orbeon.org/oxf/xml/sql");
		namespaces.put("pipeline", "java:org.orbeon.oxf.processor.pipeline.PipelineFunctionLibrary");
		namespaces.put("fr", "http://orbeon.org/oxf/xml/form-runner");
		namespaces.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");
		namespaces.put("xxforms", "http://orbeon.org/oxf/xml/xforms");
		namespaces.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
		namespaces.put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI); 
	}

	@Override
	public String getNamespaceURI(String arg0) {
		return namespaces.get(arg0);
	}

	@Override
	public String getPrefix(String arg0) {
		for (String key : namespaces.keySet())
			if (arg0.equals(namespaces.get(key)))
				return key;
		return null;
	}

	@Override
	public Iterator<String> getPrefixes(String arg0) {
		return namespaces.keySet().iterator();
	}
}
