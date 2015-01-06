package net.sf.taverna.t2.activities.rest;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

/**
 * 
 * @author Sergejs Aleksejevs
 */
public class ApacheHttpClientUsageTest extends JFrame implements ActionListener
{
  private JComboBox cbHttpMethod;
  private JTextField tfAddressBar;
  private JButton bGo;
  private JTextArea taResponse;
  private JScrollPane spResponse;
  
  
  /**
   * Constructor is solely involved in the UI initialisation.
   */
  public ApacheHttpClientUsageTest()
  {
    Container contentPane = this.getContentPane();
    contentPane.setLayout(new BorderLayout(5,5));
    
    bGo = new JButton("GO!");
    bGo.addActionListener(this);
    bGo.setDefaultCapable(true);
    this.getRootPane().setDefaultButton(bGo);
    
    tfAddressBar = new JTextField(50);
    tfAddressBar.setPreferredSize(new Dimension(0,bGo.getPreferredSize().height));
    tfAddressBar.setText("http://test.biocatalogue.org/");
    
    JPanel jpAddressBar = new JPanel();
    jpAddressBar.add(tfAddressBar);
    jpAddressBar.add(bGo);
    
    cbHttpMethod = new JComboBox(RESTActivity.HTTP_METHOD.values());
    cbHttpMethod.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(5, 5, 5, 5),
        cbHttpMethod.getBorder()));
    
    JPanel jpAllNavigation = new JPanel(new BorderLayout());
    jpAllNavigation.add(jpAddressBar, BorderLayout.NORTH);
    jpAllNavigation.add(cbHttpMethod, BorderLayout.CENTER);
    
    contentPane.add(jpAllNavigation, BorderLayout.NORTH);
    
    taResponse = new JTextArea(20, 20);
    taResponse.setEditable(true);
    
    spResponse = new JScrollPane(taResponse);
    spResponse.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEmptyBorder(0, 5, 5, 5),
        BorderFactory.createEtchedBorder()));
    contentPane.add(spResponse, BorderLayout.CENTER);
    
    this.pack();
    this.setLocationRelativeTo(null); // center on screen
  }
  
  
  /**
   * Click handler for the only button there is - the "GO!" button 
   */
  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource().equals(bGo))
    {
      try { 
        switch ((RESTActivity.HTTP_METHOD)cbHttpMethod.getSelectedItem()) {
          case GET:    doGET(); break;
          case POST:   doPOST(); break;
          case PUT:    doPUT(); break;
          case DELETE: doDELETE(); break;
        }
      }
      catch (Exception ex) {
        taResponse.setText(ex + "\n\n" + ex.getStackTrace());
      }
    }
  }
  
  
  private void doGET() {
    HttpGet httpGet = new HttpGet(tfAddressBar.getText());
    httpGet.addHeader("Accept", "application/xml");
    performHTTPRequest(httpGet);
  }
  
  
  private void doPOST() throws UnsupportedEncodingException {
//    // POST TO MYEXPERIMENT - basic auth
//    HttpPost httpPost = new HttpPost("http://sandbox.myexperiment.org/comment.xml");
//    httpPost.addHeader("Authorization", "Basic " + new String(Base64.encodeBase64(("LOGIN" + ":" + "PASSWORD").getBytes())));
//    httpPost.addHeader("Accept", "application/xml");
//    httpPost.addHeader("Content-Type", "application/xml");
//    httpPost.setEntity(new StringEntity("<comment><subject resource=\"http://sandbox.myexperiment.org/files/226\"/><comment>1234567</comment></comment>"));
//    performHTTPRequest(httpPost);
    
    // POST TO BIOCATALOGUE - no auth
    HttpPost httpPost = new HttpPost(tfAddressBar.getText());
    httpPost.addHeader("Accept", "application/xml");
    httpPost.addHeader("Content-Type", "application/xml");
    httpPost.setEntity(new StringEntity("<searchByData><searchType>input</searchType><limit>20</limit><data>test</data></searchByData>"));
    performHTTPRequest(httpPost);
  }
  
  
  private void doPUT() throws UnsupportedEncodingException {
    HttpPut httpPut = new HttpPut("http://sandbox.myexperiment.org/comment.xml?id=251");
    httpPut.addHeader("Authorization", "Basic " + new String(Base64.encodeBase64(("LOGIN" + ":" + "PASSWORD").getBytes())));
    httpPut.addHeader("Accept", "application/xml");
    httpPut.addHeader("Content-Type", "application/xml");
    httpPut.setEntity(new StringEntity("<comment><subject resource=\"http://sandbox.myexperiment.org/files/226\"/><comment>12345678</comment></comment>"));
    performHTTPRequest(httpPut);
  }
  
  
  private void doDELETE() {
    HttpDelete httpDelete = new HttpDelete("http://sandbox.myexperiment.org/comment.xml?id=251");
    httpDelete.addHeader("Authorization", "Basic " + new String(Base64.encodeBase64(("LOGIN" + ":" + "PASSWORD").getBytes())));
    httpDelete.addHeader("Accept", "application/xml");
    performHTTPRequest(httpDelete);
  }
  
  
  private void performHTTPRequest(HttpRequestBase httpRequest) {
    try {
      StringBuilder responseStr = new StringBuilder();
      // ---------------------------------------------
      
      HttpClient httpClient = new DefaultHttpClient();
      HttpContext localContext = new BasicHttpContext();
      
      HttpResponse response = httpClient.execute(httpRequest, localContext);
      // ---
      // TRACK WHERE THE FINAL REDIRECT ENDS UP - target host + URI
      HttpHost target = (HttpHost) localContext.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
      HttpUriRequest req = (HttpUriRequest) localContext.getAttribute(ExecutionContext.HTTP_REQUEST);

      responseStr.append("Final request URI: " + req.getMethod() + " " + target + req.getURI() + "\n");
//      System.out.println("Target host: " + target);
//      System.out.println("Final request URI: " + req.getURI());
//      System.out.println("Final request method: " + req.getMethod());
      // ---
      responseStr.append(response.getStatusLine() + "\n");
      
      HttpEntity entity = response.getEntity();
      responseStr.append(entity.getContentType() + "\n\n");
      
      if (entity != null) {
        InputStream in = entity.getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        
        String str;
        while ((str = reader.readLine()) != null) {
          responseStr.append(str + "\n");
        }
        
        taResponse.setText(responseStr.toString());
        taResponse.setCaretPosition(0);
      }
      
      httpClient.getConnectionManager().shutdown();
    }
    catch (Exception ex) {
      taResponse.setText(ex.getMessage() + "\n\n" + ex.getStackTrace());
    }
  }
  
  
  public static void main(String[] args)
  {
    ApacheHttpClientUsageTest frame = new ApacheHttpClientUsageTest();
    frame.setVisible(true);
    frame.tfAddressBar.setCaretPosition(frame.tfAddressBar.getText().length());
  }
  
}
