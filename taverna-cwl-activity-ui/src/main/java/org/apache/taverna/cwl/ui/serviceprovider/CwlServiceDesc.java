package org.apache.taverna.cwl.ui.serviceprovider;

import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class CwlServiceDesc extends ServiceDescription<CwlActivityConfigurationBean> {

	private Map cwlConfiguration;
	@Override
	public Class<? extends Activity<CwlActivityConfigurationBean>> getActivityClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CwlActivityConfigurationBean getActivityConfiguration() {
		//Creating the CWL configuration bean
		CwlActivityConfigurationBean configurationBean = new CwlActivityConfigurationBean();
		configurationBean.setCwlConfigurations(cwlConfiguration);
		return configurationBean;
	}

	public Map getCwlConfiguration() {
		return cwlConfiguration;
	}

	public void setCwlConfiguration(Map cwlConfiguration) {
		this.cwlConfiguration = cwlConfiguration;
	}

	@Override
	public Icon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<? extends Comparable> getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<? extends Object> getIdentifyingData() {
		// TODO Auto-generated method stub
		return null;
	}

}
