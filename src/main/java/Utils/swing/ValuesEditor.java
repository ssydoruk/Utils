/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils.swing;

import com.jidesoft.dialog.*;
import static com.jidesoft.dialog.StandardDialog.RESULT_AFFIRMED;
import static com.jidesoft.dialog.StandardDialog.RESULT_CANCELLED;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.logging.*;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import org.slf4j.Logger;
import org.slf4j.*;

/**
 *
 * @author stepan_sydoruk
 */
public class ValuesEditor extends StandardDialog {

    final static Logger logger = LoggerFactory.getLogger(ValuesEditor.class);
    private int closeCause = JOptionPane.CANCEL_OPTION;
    private final JTable tab;
    private final String selectedFormat;
    private final TableColumnAdjuster tca;
    private JButton upButton;
    private JButton downButton;
    private boolean dataChanged;
    private IAddChoices addChoices = null;
    ButtonPanel buttonPanel;
    JButton cancelButton;
    JButton addButton;
    JButton editButton;
    JButton deleteButton;
    EditValuesDialog editDialog = null;
    private DefaultTableModel infoTableModel;
    private FieldProfile[] fieldProfiles;

    public FieldProfile[] getFieldProfiles() {
        return fieldProfiles;
    }

    public ValuesEditor(Window parent, String title, String selectedFormat, FieldProfile... fieldProfiles) {
        super(parent, title);
        this.tab = new JTable();
        this.selectedFormat = selectedFormat;
        tca = new TableColumnAdjuster(tab);
        tca.setColumnHeaderIncluded(true);
        dataChanged = false;
        this.fieldProfiles = fieldProfiles;
    }

    public ValuesEditor(Window parent, String title, String selectedFormat) {
        this(parent, title, selectedFormat, null);
    }

    public int getCloseCause() {
        return closeCause;
    }

    public void setCloseCause(int closeCause) {
        this.closeCause = closeCause;
    }

    private void selectionChanged() {
        int[] selectedRows = tab.getSelectedRows();
        boolean singleSelection = selectedRows != null && selectedRows.length == 1;
        editButton.setEnabled(singleSelection);
        deleteButton.setEnabled(singleSelection);
        boolean moreThanOneSelected = selectedRows != null && selectedRows.length > 0;
        upButton.setEnabled(moreThanOneSelected && selectedRows[0] > 0);
        downButton.setEnabled(moreThanOneSelected && selectedRows[selectedRows.length - 1] < tab.getRowCount() - 1);
    }

    public IAddChoices getAddChoices() {
        return addChoices;
    }

    public void setAddChoices(IAddChoices addChoices) {
        this.addChoices = addChoices;
    }

    /**
     *
     * @return true if data was changed
     */
    public boolean doShow() {
        setModal(true);

        tca.adjustColumns();
        pack();
        if (logger.isTraceEnabled()) {
            logger.trace("Show info PanelDialog; title=" + getTitle() + "; tab cols:" + tab.getColumnCount() + " rows: " + tab.getRowCount());
            StringBuilder s = new StringBuilder(512);
            for (int i = 0; i < tab.getRowCount(); i++) {
                s.setLength(0);
                for (int j = 0; j < tab.getColumnCount(); j++) {
                    s.append("[").append(tab.getValueAt(i, j)).append("],");
                }
                logger.trace(s.toString());
            }

        }

//            ScreenInfo.CenterWindow(this);
        this.setLocationRelativeTo(getParent());
        dataChanged = false;
        setVisible(
                true);
        logger.info("Utils: " + getDialogResult());
        return dataChanged;
    }

    @Override
    public JComponent createBannerPanel() {
        return null;
    }

    @Override
    public JComponent createContentPanel() {
//                        JPanel panel = new JPanel(new BorderLayout(10, 10));
//            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

//            panel.add(mainPanel, BorderLayout.CENTER);
//            return panel;
        JScrollPane jScrollPane = new JScrollPane(tab);
        tab.getTableHeader().setVisible(true);

        JPanel listPane = new JPanel(new BorderLayout(10, 10));

        listPane.add(new JPanel(new BorderLayout()).add(jScrollPane));
        return listPane;
    }

    @Override
    public ButtonPanel createButtonPanel() {
        buttonPanel = new ButtonPanel();

        addButton = new JButton();
        buttonPanel.addButton(addButton);
        addButton.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addValuePressed(e);

            }

        });
        addButton.setText("Add");

        editButton = new JButton();
        buttonPanel.addButton(editButton);
        editButton.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editValuePressed(e);

            }

        });
        editButton.setText("Edit");

        deleteButton = new JButton();
        buttonPanel.addButton(deleteButton);
        deleteButton.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteValuePressed(e);

            }

        });
        deleteButton.setText("Delete");

        upButton = new JButton();
        buttonPanel.addButton(upButton);
        upButton.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                upPressed(e);

            }

        });
        upButton.setText("Up");

        downButton = new JButton();
        buttonPanel.addButton(downButton);
        downButton.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downPressed(e);

            }

        });
        downButton.setText("Down");

        cancelButton = new JButton();
        buttonPanel.addButton(cancelButton);

        cancelButton.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogResult(RESULT_CANCELLED);
                setCloseCause(JOptionPane.CANCEL_OPTION);
                setVisible(false);
                dispose();
            }
        });
        cancelButton.setText("Close");

        tab.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                selectionChanged();
            }
        });
        selectionChanged();

        String act = "Cancel";

        tab.getInputMap().put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), act);
        tab.getActionMap().put(act, cancelButton.getAction());

        setDefaultCancelAction(cancelButton.getAction());
        setDefaultAction(cancelButton.getAction());
        getRootPane().setDefaultButton(cancelButton);

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.setSizeConstraint(ButtonPanel.NO_LESS_THAN); // since the checkbox is quite wide, we don't want all of them have the same size.
        return buttonPanel;
    }

    private EditValuesDialog getEditDialog() {
        if (editDialog == null) {
            editDialog = new EditValuesDialog(this);
        }
        return editDialog;
    }

    private ArrayList<ArrayList<String>> getAllValues() {
        ArrayList<ArrayList<String>> ret = new ArrayList<>(tab.getColumnCount());

        for (int j = 0; j < tab.getColumnCount(); j++) {
            ArrayList<String> r = new ArrayList<>(tab.getRowCount());
            for (int i = 0; i < tab.getRowCount(); i++) {
                Object valueAt = tab.getValueAt(i, j);
                if (valueAt instanceof StringValue) {
                    String sVal = ((StringValue) valueAt).getValue();
                    if (!r.contains(sVal)) {
                        r.add(sVal);
                    }
                }

            }
            ret.add(r);
        }

        return ret;
    }

    public void moveUpwards() {
        moveRowBy(-1);
    }

    public void moveDownwards() {
        moveRowBy(1);
    }

    private void moveRowBy(int by) {
        DefaultTableModel model = (DefaultTableModel) tab.getModel();
        int[] rows = tab.getSelectedRows();
        int destination = rows[0] + by;
        int rowCount = model.getRowCount();

        if (destination < 0 || destination >= rowCount) {
            return;
        }

        model.moveRow(rows[0], rows[rows.length - 1], destination);
        tab.setRowSelectionInterval(rows[0] + by, rows[rows.length - 1] + by);
        dataChanged = true;
    }

    private void upPressed(ActionEvent e) {
        moveUpwards();

    }

    private void downPressed(ActionEvent e) {
        moveDownwards();
    }

    private EditableValue[] getEditedValues(ArrayList<Object> valsToEdit) {
        ArrayList<Object> vals;
        if ((vals = getEditDialog().doShow(valsToEdit)) != null) {
            EditableValue[] newVals = new EditableValue[fieldProfiles.length];
            for (int i = 0; i < fieldProfiles.length; i++) {
                try {
                    newVals[i] = fieldProfiles[i].getNewObjType().newInstance();
                    newVals[i].setValue(vals.get(i));
                } catch (InstantiationException ex) {
                    java.util.logging.Logger.getLogger(ValuesEditor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    java.util.logging.Logger.getLogger(ValuesEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return newVals;

        }
        return null;
    }

    private void addValuePressed(ActionEvent e) {
        EditableValue[] vals;
        if ((vals = getEditedValues(null)) != null) {
            infoTableModel.addRow(vals);
            dataChanged = true;
        }

    }

    private void editValuePressed(ActionEvent e) {
        int selectedRow = tab.getSelectedRow();
        if (selectedRow >= 0) {
            ArrayList<EditableValue> vals = new ArrayList<>(tab.getColumnCount());
            for (int i = 0; i < tab.getColumnCount(); i++) {
                vals.add((EditableValue) tab.getValueAt(selectedRow, i));
            }
            EditableValue[] newVals;
            if ((newVals = getEditedValues(null)) != null) {
                for (int i = 0; i < newVals.length; i++) {
                    infoTableModel.setValueAt(newVals[i], selectedRow, i);
                }
                dataChanged = true;
            }

        }
    }

    private void deleteValuePressed(ActionEvent e) {
        int selectedRow = tab.getSelectedRow();
        if (selectedRow >= 0) {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < tab.getColumnCount(); i++) {
                if (s.length() > 0) {
                    s.append(" - ");
                }
                s.append( tab.getValueAt(selectedRow, i));
            }
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete\n" + s, "Please confirm", YES_NO_OPTION, QUESTION_MESSAGE)
                    == JOptionPane.YES_OPTION) {
                infoTableModel.removeRow(selectedRow);
                dataChanged = true;

            }

        }
    }

    private boolean doShow(String theTitle) {
        this.setTitle(theTitle);
        return doShow();
    }

    public void setData(ArrayList<EditableValue[]> values) {
        infoTableModel = new DefaultTableModel();
        for (FieldProfile profile : fieldProfiles) {
            infoTableModel.addColumn(profile.getName());
        }
        for (EditableValue[] value : values) {
            infoTableModel.addRow(value);
        }
        tab.setModel(infoTableModel);
    }

    public void setData(Object[] columns, ArrayList<EditableValue[]> values) {
        fieldProfiles = new FieldProfile[columns.length];
        for (int i = 0; i < columns.length; i++) {
            fieldProfiles[i] = new FieldProfile((String) columns[i], EditType.COMBOBOX, StringValue.class);

        }
        setData(values);
    }

    public ArrayList<EditableValue[]> getData() {
        ArrayList<EditableValue[]> ret = new ArrayList<>(infoTableModel.getRowCount());
        for (int i = 0; i < infoTableModel.getRowCount(); i++) {
            EditableValue[] vals = new EditableValue[infoTableModel.getColumnCount()];
            for (int j = 0; j < infoTableModel.getColumnCount(); j++) {
                vals[j] = (EditableValue) infoTableModel.getValueAt(i, j);
            }
            ret.add(vals);

        }

        return ret;
    }

    public interface IAddChoices {

        HashSet<String> getAddChoices();

    }

    class EditValuesDialog extends StandardDialog {

        ArrayList col;
        ArrayList<EnterPanel> pan;

        public EditValuesDialog() {
            super();
        }

        private EditValuesDialog(ValuesEditor valuesEditor) {
            super(valuesEditor);
            col = new ArrayList<>(valuesEditor.getFieldProfiles().length);
            pan = new ArrayList<>(valuesEditor.getFieldProfiles().length);
            for (int i = 0; i < valuesEditor.getFieldProfiles().length; i++) {
                col.add(valuesEditor.getFieldProfiles()[i].getName());
                pan.add(new EnterPanel(valuesEditor.getFieldProfiles()[i]));
            }

        }

        private ArrayList<Object> doShow(ArrayList<Object> vals) {
            if (vals != null) {
                setTitle("Edit entry");
            } else {
                setTitle("New entry");
            }
            for (int i = 0; i < pan.size(); i++) {
                EnterPanel enterPanel = pan.get(i);
                addChoices(enterPanel, i);
                enterPanel.setValue((vals != null) ? vals.get(i) : null);
            }

            return doShow();
        }

        private void addChoices(EnterPanel enterPanel, int col) {
//            enterPanel.clearChoices();
            HashSet<String> ch = new HashSet<>();
            IAddChoices addChoices1 = getAddChoices();
            if (addChoices1 != null) {
                ch.addAll(addChoices1.getAddChoices());
            }
            ArrayList<ArrayList<String>> allValues = getAllValues();
            if (allValues != null && allValues.size() >= col - 1) {
                ch.addAll(allValues.get(col));
            }
            ArrayList<String> sorted = new ArrayList<>(ch);
            Collections.sort(sorted);
            enterPanel.addChoices(sorted);
        }

        @Override
        public JComponent createBannerPanel() {
            return null;
        }

        @Override
        public JComponent createContentPanel() {
            JPanel content = new JPanel();
            content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
            for (int i = 0; i < pan.size(); i++) {
                content.add(pan.get(i).getEnterPanel());

            }

            return content;
        }

        @Override
        public ButtonPanel createButtonPanel() {
            ButtonPanel buttonPanel = new ButtonPanel();
            JButton cancelButton = new JButton();
            buttonPanel.addButton(cancelButton);

            cancelButton.setAction(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setDialogResult(RESULT_CANCELLED);
                    setVisible(false);
                    dispose();
                }
            });
            cancelButton.setText("Close");

            JButton jbOK = new JButton("OK");
            buttonPanel.addButton(jbOK);

//            listPane.add(jbFilter);
            jbOK.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setDialogResult(RESULT_AFFIRMED);
                    dispose();
                }
            });

            String act = "OK";

            setDefaultCancelAction(cancelButton.getAction());
            setDefaultAction(jbOK.getAction());
            getRootPane().setDefaultButton(jbOK);

            buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            buttonPanel.setSizeConstraint(ButtonPanel.NO_LESS_THAN); // since the checkbox is quite wide, we don't want all of them have the same size.
            return buttonPanel;
        }

        public ArrayList<Object> doShow() {

//            setModal(true);
            pack();

//            ScreenInfo.CenterWindow(this);
            setLocationRelativeTo(getParent());
//            setVisible(true);
            setAlwaysOnTop(true);
            java.awt.EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    toFront();

                }
            });
//            setVisible(false);
            setVisible(true);

            if (getDialogResult() == StandardDialog.RESULT_AFFIRMED) {
                ArrayList<Object> ret = new ArrayList<>(pan.size());
                for (EnterPanel enterPanel : pan) {
                    ret.add(enterPanel.getValue());
                }
                return ret;

            } else {
                return null;
            }

        }

        class EnterPanel {

            private final JComponent tbValue;
            private final JPanel enterPanel;
            private final EditType editType;
            private Object value = null;

            EnterPanel(FieldProfile fieldProfile) {
                enterPanel = new JPanel();
                enterPanel.setLayout(new BoxLayout(enterPanel, BoxLayout.LINE_AXIS));
                this.editType = fieldProfile.getEditType();
                enterPanel.add(new JLabel(fieldProfile.getName()));
                switch (editType) {
                    case COMBOBOX:
                        tbValue = new JComboBox();
                        ((JComboBox) tbValue).setEditable(true);
                        break;

                    case STRING:
                        tbValue = new JTextField();
                        break;

                    case INT:
                        tbValue = new JFormattedTextField();
                        break;

                    case PASSWORD:
                        tbValue = new JPasswordField();
                        break;

                    default:
                        tbValue = null;

                }
                enterPanel.add(tbValue);
            }

            public void setValue(Object val) {
                this.value = val;
            }

            public JPanel getEnterPanel() {
                return enterPanel;
            }

            public Object getValue() {
                if (tbValue instanceof JComboBox) {
                    return (tbValue != null)
                            ? (((JComboBox) tbValue).getSelectedItem() != null)
                            ? ((JComboBox) tbValue).getSelectedItem().toString() : null : null;

                } else if (tbValue instanceof JTextField) {
                    return ((JTextField) tbValue).getText();
                } else if (tbValue instanceof JPasswordField) {
                    return ((JPasswordField) tbValue).getPassword().toString();
                } else if (tbValue instanceof JFormattedTextField) {
                    return ((JFormattedTextField) tbValue).getValue();
                }
                return null;
            }
//
//            private void clearChoices() {
//                tbValue.removeAllItems();
//
//            }

            private void addChoices(ArrayList<String> addChoices) {
                if (tbValue instanceof JComboBox) {
                    for (String addChoice : addChoices) {
                        ((JComboBox) tbValue).addItem(addChoice);
                    }

                }

            }
        }
    }

}
