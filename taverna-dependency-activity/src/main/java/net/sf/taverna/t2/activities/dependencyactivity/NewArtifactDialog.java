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
package net.sf.taverna.t2.activities.dependencyactivity;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Dialog used for entering an artifact.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class NewArtifactDialog
    extends JDialog
{
    private JTextField jtfGroupID;

	private JTextField jtfArtifactID;

    private JTextField jtfVersion;

    /** Stores new group ID entered */
    private String groupID = null;
    
    /** Stores new artifact ID entered */
    private String artifactID = null;

    /** Stores new version entered */
    private String version = null;


    /**
     * Creates new NewArtifactDialog.
     */
    public NewArtifactDialog(String sTitle, boolean bModal)
    {
        super();
        setTitle(sTitle);
        setModal(bModal);
        initComponents();
    }

    public String getGroupID()
    {
    	return groupID;
    }

    public String getArtifatcID()
    {
    	return artifactID;
    }
    
    public String getVersion()
    {
    	return version;
    }
    
    /**
     * Initialise the dialog's GUI components.
     */
    private void initComponents()
    {
        getContentPane().setLayout(new BorderLayout());

        JLabel jlGroupID = new JLabel("Group ID:");
        JLabel jlArtifactID = new JLabel("Artifact ID:");
        JLabel jlVersion = new JLabel("Version");

        jtfGroupID = new JTextField(15);
        jtfArtifactID = new JTextField(15);
        jtfVersion = new JTextField(15);

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
        
        JPanel jpPassword = new JPanel(new GridLayout(3, 2, 5, 5));
        jpPassword.add(jlGroupID);
        jpPassword.add(jtfGroupID);
        jpPassword.add(jlArtifactID);
        jpPassword.add(jtfArtifactID);
        jpPassword.add(jlVersion);
        jpPassword.add(jtfVersion);
        jpPassword.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel jpButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        jpButtons.add(jbOK);
        jpButtons.add(jbCancel);

        getContentPane().add(jpPassword, BorderLayout.CENTER);
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

    private boolean checkFields()
    {
        groupID = jtfGroupID.getText();
        artifactID = jtfArtifactID.getText();
        version = jtfVersion.getText();
        
        if ((!groupID.equals("")) && (!artifactID.equals("")) && (!version.equals(""))){
            return true;
        }
        else { 
            JOptionPane.showMessageDialog(this,
                    "Field(s) may not be empty", 
                    "ERROR",
                    JOptionPane.ERROR_MESSAGE);

                return false;
        }
    }

    /**
     * OK button pressed or otherwise activated.
     */
    private void okPressed()
    {
        if (checkFields()) {
            closeDialog();
        }
    }

    /**
     * Cancel button pressed or otherwise activated.
     */
    private void cancelPressed()
    {
    	// Set the fields to null as it might have changed in the meantime 
    	// if user entered something previously
    	groupID = null;
    	artifactID = null;
    	version = null;
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



