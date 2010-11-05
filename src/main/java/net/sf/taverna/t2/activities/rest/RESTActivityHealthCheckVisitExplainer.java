package net.sf.taverna.t2.activities.rest;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.t2.lang.ui.ReadOnlyTextArea;
import net.sf.taverna.t2.visit.VisitKind;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.workbench.report.explainer.VisitExplainer;
import net.sf.taverna.t2.workbench.report.view.ReportViewConfigureAction;
import net.sf.taverna.t2.workflowmodel.Processor;

// import status constants
import static net.sf.taverna.t2.activities.rest.RESTActivityHealthCheck.*;

/**
 * 
 * @author Sergejs Aleksejevs
 */
public class RESTActivityHealthCheckVisitExplainer implements VisitExplainer
{
  
  public boolean canExplain(VisitKind vk, int resultId) {
    return (vk instanceof RESTActivityHealthCheck);
  }
  
  
  /**
   * This class only handles {@link VisitReport} instances that are of
   * {@link RESTActivityHealthCheck} kind. Therefore, decisions on
   * the explanations / solutions are made solely by visit result IDs.
   */
  public JComponent getExplanation(VisitReport vr)
  {
    int resultId = vr.getResultId();
    String explanation = null;
    
    switch (resultId) {
      case CORRECTLY_CONFIGURED:
        explanation = "No problem found"; break;
        
      case GENERAL_CONFIG_PROBLEM:
        explanation = "Configuration of this REST activity is not valid"; break;
        
      default:
        explanation = "Unknown issue - no expalanation available"; break;
    }
    
    return new ReadOnlyTextArea(explanation);
  }
  
  
  
  /**
   * This class only handles {@link VisitReport} instances that are of
   * {@link RESTActivityHealthCheck} kind. Therefore, decisions on
   * the explanations / solutions are made solely by visit result IDs.
   */
  public JComponent getSolution(VisitReport vr)
  {
    int resultId = vr.getResultId();
    String explanation = null;
    boolean includeConfigButton = false;
    
    switch (resultId) {
      case CORRECTLY_CONFIGURED:
        explanation = "No change necessary"; break;
        
      case GENERAL_CONFIG_PROBLEM:
        explanation = "Please check configuration of this REST activity:";
                      includeConfigButton = true;
                      break;
      
      default:
        explanation = "Unknown issue - no solution available"; break;
    }
    
    
    JPanel jpSolution = new JPanel();
    jpSolution.setLayout(new BoxLayout(jpSolution, BoxLayout.Y_AXIS));
    
    ReadOnlyTextArea taExplanation = new ReadOnlyTextArea(explanation);
    taExplanation.setAlignmentX(Component.LEFT_ALIGNMENT);
    jpSolution.add(taExplanation);
    
    if (includeConfigButton)
    {
      JButton button = new JButton();
      Processor p = (Processor) (vr.getSubject());
      button.setAction(new ReportViewConfigureAction(p));
      button.setText("Open REST Activity configuration dialog");
      button.setAlignmentX(Component.LEFT_ALIGNMENT);
      
      jpSolution.add(button);
    }
    
    
    return (jpSolution);
  }
  
}
