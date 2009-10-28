/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.wsdl.security;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

/**
 * Dialog for entering user's username and password for the WSDL service.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class GetPasswordDialog
    extends JDialog
{
	// Whether we should ask user to save their username and password using Credential Manager
	private boolean shouldAskUserToSave;

    // Username field 
    private JTextField jtfUsername;

    // Password field 
    private JPasswordField jpfPassword;
    
    // Whether user wished to save the username and password using Credential Manager
    private JCheckBox jcbSave;
   
    // Stores username entered
    private String username;
    
    // Stores password entered
    private String password;

    // WSDL service url
	//private String wsdlURL;

	private String instructions;


    public GetPasswordDialog(String instructions, boolean shouldAskUserToSave)
    {
        super((Frame)null, "Enter username and password", true);
        //this.wsdlURL = wsdlURL;
        this.instructions = instructions;
        this.shouldAskUserToSave = shouldAskUserToSave;
        initComponents();
    } 
    
    /**
     * Initialise the dialog's GUI components.
     */
    private void initComponents()
    {
        getContentPane().setLayout(new BorderLayout());

        JTextArea jtaInstructions = new JTextArea(instructions);
        jtaInstructions.setEditable(false);
        //jtaInstructions.setBackground(this.getBackground());
        jtaInstructions.setFont(new Font(null, Font.PLAIN, 11));
        jtaInstructions.setBorder(new EmptyBorder(5, 5, 5, 5));
        JPanel jpInstructions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jpInstructions.add(jtaInstructions);
        
        JLabel jlUsername = new JLabel("Username");
        jlUsername.setBorder(new EmptyBorder(5, 5, 5, 5));
        JLabel jlPassword = new JLabel("Password");
        jlPassword.setBorder(new EmptyBorder(5, 5, 5, 5));
              
        jtfUsername = new JTextField(15);
        jpfPassword = new JPasswordField(15);
        
        JButton jbOK = new JButton("OK");
        jbOK.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                okPressed();
            }
        });

        JButton jbCancel = new JButton("Cancel");
        jbCancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                cancelPressed();
            }
        });

        // Central panel with username/password fields and a "Do you want to Save?" checkbox
        JPanel jpMain = new JPanel(new BorderLayout());
        
        JPanel jpPassword = new JPanel(new GridLayout(2, 2, 5, 5));
        jpPassword.add(jlUsername);
        jpPassword.add(jtfUsername);
        jpPassword.add(jlPassword);
        jpPassword.add(jpfPassword);
        jpMain.add(jpPassword, BorderLayout.CENTER);
        
        // If user wants to save this username and password
        jcbSave = new JCheckBox();
        jcbSave.setBorder(new EmptyBorder(5,5,5,5));
        jcbSave.setSelected(true);
        jcbSave.setText("Use Credential Manager to save this username and password");  
        if (shouldAskUserToSave){
        	JPanel jpSaveCheckBox = new JPanel(new FlowLayout(FlowLayout.LEFT));
        	jpSaveCheckBox.add(jcbSave);
        	jpMain.add(jpSaveCheckBox, BorderLayout.SOUTH);
        }
        
        jpPassword.setBorder(new CompoundBorder(
                new EmptyBorder(10, 10, 10, 10), new EtchedBorder()));
        
        JPanel jpButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        jpButtons.add(jbOK);
        jpButtons.add(jbCancel);

        jpPassword.setMinimumSize(new Dimension(300,100));

        getContentPane().add(jpInstructions, BorderLayout.NORTH);
        getContentPane().add(jpMain, BorderLayout.CENTER);
        getContentPane().add(jpButtons, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent evt)
            {
                closeDialog();
            }
        });

        setResizable(false);

        getRootPane().setDefaultButton(jbOK);

        pack();
    }
    
    /**
     * Get the username set in the dialog.
     *
     * @return the username
     */
    public String getUsername()
    {
        return username;
    }
    
    /**
     * Get the password set in the dialog.
     *
     * @return the password
     */
    public String getPassword()
    {
    	return password;
    }
    
    /**
     * Check if user wishes to save username and pasword
     * using Credential Manager.
     */
    public boolean shouldSaveUsernameAndPassword(){
    	return jcbSave.isSelected();
    }

    private boolean checkControls()
    {    	
    	username = jtfUsername.getText();
    	if (username.length() == 0){
            JOptionPane.showMessageDialog(this,
                "Username cannot be empty", 
                "Warning",
                JOptionPane.WARNING_MESSAGE);            
            return false;
    	}
    	   	
    	password = new String(jpfPassword.getPassword());
    	if (password.length() == 0) { // password empty
            JOptionPane.showMessageDialog(this,
                "Password cannot be empty", 
                "Warning",
                JOptionPane.WARNING_MESSAGE);

            return false;        	
        }
   	
    	return true;
    }

    /**
     * OK button pressed or otherwise activated.
     */
    private void okPressed()
    {
        if (checkControls()) {
            closeDialog();
        }
    }

    /**
     * Cancel button pressed or otherwise activated.
     */
    private void cancelPressed()
    {
    	// Set all fields to null to indicate that cancel button was pressed
    	username = null;
    	password = null;
        closeDialog();
    }

    /**
     * Close the dialog.
     */
    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }
}

