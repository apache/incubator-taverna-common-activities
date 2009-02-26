package net.sf.taverna.t2.activities.wsdl;

import java.io.IOException;
import java.util.Map;

import net.sf.taverna.t2.activities.wsdl.xmlsplitter.XMLOutputSplitterActivity;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.UnknownOperationException;

/**
 * Interface for an activity such as {@link WSDLActivity} and
 * {@link XMLOutputSplitterActivity} that can provide {@link TypeDescriptor}s for
 * it's outputs.
 * 
 * @author Stian Soiland-Reyes
 * 
 * @param <ActivityBeanType> The configuration bean type of the activity
 */

@SuppressWarnings("unchecked")
public interface OutputPortTypeDescriptorActivity {

	/**
	 * Provides access to the TypeDescriptor for a given output port name.
	 * <br>
	 * This TypeDescriptor represents the Type defined in the schema for this Activities
	 * WSDL.
	 * 
	 * @param portName
	 * @return the TypeDescriptor, or null if the portName is not recognised.
	 * @throws UnknownOperationException if the operation this Activity is associated with doesn't exist.
	 * @throws IOException
	 * 
	 * @see TypeDescriptor
	 * @see #getTypeDescriptorsForOutputPorts()
	 * @see #getTypeDescriptorForInputPort(String)
	 */
	public abstract TypeDescriptor getTypeDescriptorForOutputPort(
			String portName) throws UnknownOperationException, IOException;

	/**
	 * Return TypeDescriptor for a all output ports.
	 * <p>
	 * This TypeDescriptor represents the Type defined in the schema for this Activities
	 * WSDL.
	 * 
	 * @param portName
	 * @return A {@link Map} from portname to {@link TypeDescriptor}
	 * @throws UnknownOperationException if the operation this Activity is associated with doesn't exist.
	 * @throws IOException If the WSDL or some of its dependencies could not be read
	 * 
	 * @see TypeDescriptor
	 * @see #getTypeDescriptorForOutputPort(String)
	 * @see #getTypeDescriptorsForInputPorts()
	 */
	public abstract Map<String, TypeDescriptor> getTypeDescriptorsForOutputPorts()
			throws UnknownOperationException, IOException;

}