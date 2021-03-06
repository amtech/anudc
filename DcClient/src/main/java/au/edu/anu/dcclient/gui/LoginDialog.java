/*******************************************************************************
 * Australian National University Data Commons
 * Copyright (C) 2013  The Australian National University
 * 
 * This file is part of Australian National University Data Commons.
 * 
 * Australian National University Data Commons is free software: you
 * can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package au.edu.anu.dcclient.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.anu.dcclient.tasks.GetUserInfoTask;
import au.edu.anu.dcclient.tasks.SetAuthDefaultTask;

/**
 * This class displays a Login dialog box allowing users to enter their username and password for logging into Data
 * Commons.
 */
public class LoginDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(LoginDialog.class);

	private final Component parent;
	private int optionSelected = JOptionPane.CANCEL_OPTION;
	private String[] userInfo = null;

	private JPanel contentPanel;
	private JLabel lblUser;
	private JLabel lblPassword;
	private JTextField txtUser;
	private JTextField txtPassword;
	private JProgressBar progressBar;
	private JPanel buttonPane;
	private JButton okButton;
	private JButton cancelButton;

	/**
	 * Create the dialog.
	 */
	public LoginDialog(Component parent) {
		this.parent = parent;
		initGui();
	}

	private void initGui() {
		setResizable(false);
		setTitle("Login");
		setSize(293, 128);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setLocationRelativeTo(this.parent);
		getContentPane().setLayout(new BorderLayout());
		
		this.contentPanel = new JPanel();
		this.contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(this.contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 73, 157, 0 };
		gbl_contentPanel.rowHeights = new int[] { 20, 20, 0 };
		gbl_contentPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		this.contentPanel.setLayout(gbl_contentPanel);

		this.lblUser = new JLabel("User");
		GridBagConstraints gbc_lblUser = new GridBagConstraints();
		gbc_lblUser.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblUser.insets = new Insets(0, 0, 5, 5);
		gbc_lblUser.gridx = 0;
		gbc_lblUser.gridy = 0;
		this.contentPanel.add(this.lblUser, gbc_lblUser);

		this.txtUser = new JTextField();
		GridBagConstraints gbc_txtUser = new GridBagConstraints();
		gbc_txtUser.anchor = GridBagConstraints.NORTH;
		gbc_txtUser.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtUser.insets = new Insets(0, 0, 5, 0);
		gbc_txtUser.gridx = 1;
		gbc_txtUser.gridy = 0;
		this.contentPanel.add(this.txtUser, gbc_txtUser);
		this.txtUser.setColumns(10);

		this.lblPassword = new JLabel("Password");
		GridBagConstraints gbc_lblPassword = new GridBagConstraints();
		gbc_lblPassword.anchor = GridBagConstraints.WEST;
		gbc_lblPassword.insets = new Insets(0, 0, 0, 5);
		gbc_lblPassword.gridx = 0;
		gbc_lblPassword.gridy = 1;
		this.contentPanel.add(this.lblPassword, gbc_lblPassword);

		this.txtPassword = new JPasswordField();
		this.txtPassword.setColumns(10);
		GridBagConstraints gbc_txtPassword = new GridBagConstraints();
		gbc_txtPassword.anchor = GridBagConstraints.NORTH;
		gbc_txtPassword.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPassword.gridx = 1;
		gbc_txtPassword.gridy = 1;
		this.contentPanel.add(this.txtPassword, gbc_txtPassword);
		
		buttonPane = new JPanel();
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		this.progressBar = new JProgressBar();
		this.progressBar.setVisible(false);
		buttonPane.setLayout(new MigLayout("", "[146px][47px][65px]", "[23px]"));
		buttonPane.add(this.progressBar, "cell 0 0,alignx left,aligny center");

		okButton = new JButton("OK");
		okButton.addActionListener(new OkButtonActionListener());
		okButton.setActionCommand("OK");
		buttonPane.add(okButton, "cell 1 0,alignx left,aligny top");
		getRootPane().setDefaultButton(okButton);

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				optionSelected = JOptionPane.CANCEL_OPTION;
				LoginDialog.this.setVisible(false);
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton, "cell 2 0,alignx left,aligny top");
		
	}

	@Override
	public void setVisible(boolean b) {
		this.setLocationRelativeTo(this.parent);
		this.txtUser.requestFocusInWindow();
		this.progressBar.setIndeterminate(false);
		super.setVisible(b);
	}

	/**
	 * Displays this login dialog box.
	 * 
	 * @return Returns JOptionPane.OK_OPTION if user clicked OK, JOptionPane.CANCEL_OPTION if cancelled.
	 */
	public int display() {
		setVisible(true);
		return this.optionSelected;
	}

	/**
	 * Returns the user information as a string array with first element as username and second as display name.
	 * 
	 * @return String array
	 */
	public String[] getUserInfo() {
		return userInfo;
	}

	private class OkButtonActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					LOGGER.info("Setting credentials: User {}, Password ****.", LoginDialog.this.txtUser.getText());
					progressBar.setVisible(true);
					progressBar.setIndeterminate(true);
					optionSelected = JOptionPane.OK_OPTION;
					
					final SetAuthDefaultTask setAuthTask = new SetAuthDefaultTask(LoginDialog.this.txtUser.getText(),
							LoginDialog.this.txtPassword.getText());
					setAuthTask.execute();
					LOGGER.info("SetAuthTask Created");
					
					GetUserInfoTask getUserInfoTask = new GetUserInfoTask() {
						@Override
						protected String[] doInBackground() throws Exception {
							setAuthTask.get();
							return super.doInBackground();
						}
						
						@Override
						protected void done() {
							try {
								userInfo = get();
							} catch (Exception e) {
								userInfo = null;
							} finally {
								LoginDialog.this.setVisible(false);
							}
							super.done();
						}
					};
					getUserInfoTask.execute();
				}
			});
		}
	}
}
