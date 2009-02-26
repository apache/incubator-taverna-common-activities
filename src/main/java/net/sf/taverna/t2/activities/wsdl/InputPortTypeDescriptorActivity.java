package net.sf.taverna.t2.activities.wsdl;

import java.io.IOException;
import java.util.Map;

import net.sf.taverna.t2.activities.wsdl.xmlsplitter.XMLInputSplitterActivity;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.UnknownOperationException;

/**
 * Interface for an activity such as {@link WSDLActivity} and
 * {@link XMLInputSplitterActivity} that can provide {@link TypeDescriptor}s for
 * it's inputs.
 * 
 * @author Stian Soiland-Reyes
 * 
 * @param <ActivityBeanType> The configuration bean type of the activity
 */
@SuppressWarnings("unchecked")
public interface InputPortTypeDescriptorActivity {

	/**
	 * Provides access to the TypeDescriptor for a given input port name.
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
	 * @see #getTypeDescriptorsForInputPorts()
	 * @see #getTypeDescriptorForOutputPort(String)
	 */
	public abstract TypeDescriptor getTypeDescriptorForInputPort(String portName)
			throws UnknownOperationException, IOException;

	/**
	 * Return TypeDescriptor for a all input ports.
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
	 * @see #getTypeDescriptorForInputPort(String)
	 * @see #getTypeDescriptorsForOutputPorts()
	 */
	public abstract Map<String, TypeDescriptor> getTypeDescriptorsForInputPorts()
			throws UnknownOperationException, IOException;

}