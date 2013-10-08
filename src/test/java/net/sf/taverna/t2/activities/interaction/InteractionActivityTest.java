package net.sf.taverna.t2.activities.interaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class InteractionActivityTest {

	private InteractionActivityConfigurationBean configBean;

	private final InteractionActivity activity = new InteractionActivity();

	@Before
	public void makeConfigBean() throws Exception {
		this.configBean = new InteractionActivityConfigurationBean();
		this.configBean.setPresentationOrigin("test.vm");
	}

	@Ignore
	@Test(expected = ActivityConfigurationException.class)
	public void invalidConfiguration() throws ActivityConfigurationException {
		final InteractionActivityConfigurationBean invalidBean = new InteractionActivityConfigurationBean();
		invalidBean.setPresentationOrigin("nothing.vm");
		// Should throw ActivityConfigurationException
		this.activity.configure(invalidBean);
	}

	@Ignore
	@Test
	public void executeAsynch() throws Exception {
		this.activity.configure(this.configBean);

		final Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("firstInput", "hello");

		final Map<String, Class<?>> expectedOutputTypes = new HashMap<String, Class<?>>();
		expectedOutputTypes.put("simpleOutput", String.class);
		expectedOutputTypes.put("moreOutputs", String.class);

		final Map<String, Object> outputs = ActivityInvoker
				.invokeAsyncActivity(this.activity, inputs, expectedOutputTypes);

		assertEquals("Unexpected outputs", 2, outputs.size());
		assertEquals("simple", outputs.get("simpleOutput"));
		assertEquals(Arrays.asList("Value 1", "Value 2"),
				outputs.get("moreOutputs"));

	}

	@Ignore
	@Test
	public void reConfiguredActivity() throws Exception {
		assertEquals("Unexpected inputs", 0, this.activity.getInputPorts()
				.size());
		assertEquals("Unexpected outputs", 0, this.activity.getOutputPorts()
				.size());

		this.activity.configure(this.configBean);
		assertEquals("Unexpected inputs", 1, this.activity.getInputPorts()
				.size());
		assertEquals("Unexpected outputs", 2, this.activity.getOutputPorts()
				.size());

		this.activity.configure(this.configBean);
		// Should not change on reconfigure
		assertEquals("Unexpected inputs", 1, this.activity.getInputPorts()
				.size());
		assertEquals("Unexpected outputs", 2, this.activity.getOutputPorts()
				.size());
	}

	@Ignore
	@Test
	public void reConfiguredSpecialPorts() throws Exception {
		this.activity.configure(this.configBean);

		final InteractionActivityConfigurationBean specialBean = new InteractionActivityConfigurationBean();
		specialBean.setPresentationOrigin("test.vm");
		this.activity.configure(specialBean);
		// Should now have added the optional ports
		assertEquals("Unexpected inputs", 2, this.activity.getInputPorts()
				.size());
		assertEquals("Unexpected outputs", 3, this.activity.getOutputPorts()
				.size());
	}

	@Ignore
	@Test
	public void configureActivity() throws Exception {
		final Set<String> expectedInputs = new HashSet<String>();
		expectedInputs.add("firstInput");

		final Set<String> expectedOutputs = new HashSet<String>();
		expectedOutputs.add("simpleOutput");
		expectedOutputs.add("moreOutputs");

		this.activity.configure(this.configBean);

		final Set<ActivityInputPort> inputPorts = this.activity.getInputPorts();
		assertEquals(expectedInputs.size(), inputPorts.size());
		for (final ActivityInputPort inputPort : inputPorts) {
			assertTrue("Wrong input : " + inputPort.getName(),
					expectedInputs.remove(inputPort.getName()));
		}

		final Set<OutputPort> outputPorts = this.activity.getOutputPorts();
		assertEquals(expectedOutputs.size(), outputPorts.size());
		for (final OutputPort outputPort : outputPorts) {
			assertTrue("Wrong output : " + outputPort.getName(),
					expectedOutputs.remove(outputPort.getName()));
		}
	}
}
