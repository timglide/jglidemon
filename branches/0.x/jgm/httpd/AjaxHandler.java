package jgm.httpd;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*; 

import java.io.*;
import java.util.Properties;

import jgm.JGlideMon;
import jgm.HTTPD;
import jgm.gui.updaters.StatusUpdater;

public class AjaxHandler extends Handler {
	public static final String DEF_ERROR_XML = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\nn" +
		"<response>\n" + 
		"<status>failure</status>\n" +
		"<message/>\n" +
		"</response>";
	
	@Override
	public Response handle(String uri, String method, Properties headers, Properties params) {
		String out = createXML(uri, method, headers, params);
		if (out == null) out = DEF_ERROR_XML;
		
		Response ret = null;
		
		ret = new Response(HTTPD.HTTP_OK, HTTPD.MIME_HTML, out);
		ret.addHeader("Content-type", "text/xml");
		
		return ret;
	}

	private String createXML(String uri, String method, Properties headers, Properties params) {
		Document xml = null;
		DocumentBuilderFactory factory = 
		DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			xml = builder.newDocument();  // Create from whole cloth
	
			Element root = xml.createElement("response"); 
			xml.appendChild(root);
			
			// status
			Element tmp = xml.createElement("status");
			Text status = xml.createTextNode("success");
			tmp.appendChild(status);
			root.appendChild(tmp);
			
			// for an error message
			tmp = xml.createElement("message");
			Text message = xml.createTextNode("");
			tmp.appendChild(message);
			root.appendChild(tmp);
			
			
			Element request = xml.createElement("request");
			root.appendChild(request);
			
			// url
			tmp = xml.createElement("url");
			tmp.appendChild(xml.createTextNode(uri));
			request.appendChild(tmp);
	
			// headers
			tmp = xml.createElement("headers");
			
			for (Object key : headers.keySet()) {
				Element tmp2 = xml.createElement("header");
				tmp2.setAttribute("name", key.toString());
				tmp2.appendChild(xml.createTextNode(headers.get(key).toString()));
				tmp.appendChild(tmp2);
			}
			
			request.appendChild(tmp);
			
			
			if (uri.equals("status")) {
				// jgm info
				Element jgm_ = xml.createElement("jgm");
				tmp = xml.createElement("version");
				tmp.appendChild(xml.createTextNode(JGlideMon.version));
				jgm_.appendChild(tmp);
				
				boolean connected = jgm.glider.Connector.isConnected();
				tmp = xml.createElement("connected");
				tmp.appendChild(xml.createTextNode(Boolean.toString(connected)));
				jgm_.appendChild(tmp);
				
				root.appendChild(jgm_);
				
				
				// glider info
				Element glider = xml.createElement("glider");
				
				if (connected && StatusUpdater.instance != null) {
					StatusUpdater s = StatusUpdater.instance;
					tmp = xml.createElement("version");
					tmp.appendChild(xml.createTextNode(s.version));
					// ...
				}
				
				root.appendChild(glider);
			}
			
			
			// prepare for serialization
			xml.setXmlStandalone(true);
			xml.getDocumentElement().normalize();
			
//			Serialisation through Tranform.
			StringWriter out = new StringWriter();
			DOMSource domSource = new DOMSource(xml);
			StreamResult streamResult = new StreamResult(out);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer serializer = tf.newTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
//			serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"users.dtd");
			serializer.setOutputProperty(OutputKeys.INDENT,"yes");
			serializer.transform(domSource, streamResult); 
			
			return out.toString();
		} catch (Throwable pce) {
			// this had better not happen
			
			// Parser with specified options can't be built
			pce.printStackTrace();
			System.exit(1);
		}
		
		return null;
	}
}
