/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import static com.jidesoft.dialog.StandardDialog.RESULT_CANCELLED;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author stepan_sydoruk
 */
public class InfoPanel extends StandardDialog {

    private Container mainPanel;
    private final int buttonOptions;
    private JComponent bannerPannel = null;

    private InfoPanel(Window p, String t, JComponent bannerPannel, Container jScrollPane, int YES_NO_OPTION) {
        this(p, t, jScrollPane, YES_NO_OPTION);
        this.bannerPannel = bannerPannel;
    }

    public void setMainPanel(Container mainPanel) {
        this.mainPanel = mainPanel;
    }

    public InfoPanel(Window parent, String title, Container jScrollPane, int buttonOptions) throws HeadlessException {
        super(parent, title);
        this.mainPanel = jScrollPane;
        this.buttonOptions = buttonOptions;
    }

    @Override
    public JComponent createBannerPanel() {
//            return new BannerPanel("pannel tytle", "descrr");
//            if( bannerPannel!=null){
//                bannerPannel = new BannerPanel("pannel tytle", "descrr");
//                return bannerPannel;
//            }
        return bannerPannel;
    }

    ButtonPanel buttonPanel = new ButtonPanel();

    @Override
    public JComponent createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        panel.add(mainPanel, BorderLayout.CENTER);
        return panel;
    }

    @Override
    public ButtonPanel createButtonPanel() {

        switch (buttonOptions) {

            case JOptionPane.DEFAULT_OPTION: {
                JButton cancelButton = new JButton();
                buttonPanel.addButton(cancelButton);

                cancelButton.setAction(new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        setDialogResult(RESULT_CANCELLED);
                        setVisible(false);
                        dispose();
                    }
                });
                cancelButton.setText("Close");

                setDefaultCancelAction(cancelButton.getAction());
                getRootPane().setDefaultButton(cancelButton);
                break;
            }

            case JOptionPane.YES_NO_CANCEL_OPTION: {

                JButton yesButton = new JButton();
                buttonPanel.addButton(yesButton);

                yesButton.setAction(new AbstractAction("Yes") {
                    public void actionPerformed(ActionEvent e) {
                        setDialogResult(JOptionPane.YES_OPTION);
                        setVisible(false);
                        dispose();
                    }
                });

                JButton noButton = new JButton();
                buttonPanel.addButton(noButton);

                noButton.setAction(new AbstractAction("No") {
                    public void actionPerformed(ActionEvent e) {
                        setDialogResult(JOptionPane.NO_OPTION);
                        setVisible(false);
                        dispose();
                    }
                });

                JButton cancelButton = new JButton();
                buttonPanel.addButton(cancelButton);

                cancelButton.setAction(new AbstractAction("Cancel") {
                    public void actionPerformed(ActionEvent e) {
                        setDialogResult(JOptionPane.CANCEL_OPTION);
                        setVisible(false);
                        dispose();
                    }
                });

                setDefaultCancelAction(cancelButton.getAction());
                getRootPane().setDefaultButton(noButton);
                break;
            }

            case JOptionPane.YES_NO_OPTION: {
                JButton yesButton = new JButton();
                buttonPanel.addButton(yesButton);

                yesButton.setAction(new AbstractAction("Yes") {
                    public void actionPerformed(ActionEvent e) {
                        setDialogResult(JOptionPane.YES_OPTION);
                        setVisible(false);
                        dispose();
                    }
                });

                JButton noButton = new JButton();
                buttonPanel.addButton(noButton);

                noButton.setAction(new AbstractAction("No") {
                    public void actionPerformed(ActionEvent e) {
                        setDialogResult(JOptionPane.NO_OPTION);
                        setVisible(false);
                        dispose();
                    }
                });
                setDefaultCancelAction(noButton.getAction());
                getRootPane().setDefaultButton(noButton);
                break;
            }
            case JOptionPane.OK_CANCEL_OPTION: {
                JButton yesButton = new JButton();
                buttonPanel.addButton(yesButton);

                yesButton.setAction(new AbstractAction("OK") {
                    public void actionPerformed(ActionEvent e) {
                        setDialogResult(JOptionPane.OK_OPTION);
                        setVisible(false);
                        dispose();
                    }
                });

                JButton noButton = new JButton();
                buttonPanel.addButton(noButton);

                noButton.setAction(new AbstractAction("Cancel") {
                    public void actionPerformed(ActionEvent e) {
                        setDialogResult(JOptionPane.CANCEL_OPTION);
                        setVisible(false);
                        dispose();
                    }
                });
                setDefaultCancelAction(noButton.getAction());
                getRootPane().setDefaultButton(noButton);
                break;

            }
        }
//            buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setSizeConstraint(ButtonPanel.NO_LESS_THAN); // since the checkbox is quite wide, we don't want all of them have the same size.
        return buttonPanel;
    }

    public void addButton(String name, int action) {
        JButton newButton = new JButton();
        buttonPanel.addButton(newButton);

        newButton.setAction(new AbstractAction(name) {
            public void actionPerformed(ActionEvent e) {
//                LogManager.getLogger().info("setting action: "+action);
                setDialogResult(action);
                setVisible(false);
                dispose();
            }
        });
        buttonPanel.add(newButton);
    }

    public void showModal() {
        pack();
//        ScreenInfo.CenterWindow(allFiles);
        setModal(true);
        ScreenInfo.setVisible(getParent(), this, true);
    }
}
