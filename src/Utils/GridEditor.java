/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import Utils.ScreenInfo;
import Utils.TableColumnAdjuster;
import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;
import static com.jidesoft.dialog.StandardDialog.RESULT_AFFIRMED;
import static com.jidesoft.dialog.StandardDialog.RESULT_CANCELLED;
import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author stepan_sydoruk
 */
public class GridEditor extends StandardDialog {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger();

    private int closeCause = JOptionPane.CANCEL_OPTION;
    private JTable tab;
    private String selectedFormat;
    private TableColumnAdjuster tca;
    private boolean dataChanged;
    private Window theParent;
    private int mandatoryColumns = 0;

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
    }

    private IAddChoices addChoices = null;

    public IAddChoices getAddChoices() {
        return addChoices;
    }

    public void setAddChoices(IAddChoices addChoices) {
        this.addChoices = addChoices;
    }

    private void importFile(String pathToCsv) {
        BufferedReader csvReader = null;
        String delim = null;
        try {
            logger.info(pathToCsv);
            csvReader = new BufferedReader(new FileReader(pathToCsv));
            String row;

            boolean continueLoad = false;
            if ((row = csvReader.readLine()) != null) {
                delim = guessDelimiter(row);
                if (delim == null) {
                    JOptionPane.showMessageDialog(this, "Not able to determine type of csv file\n"
                            + pathToCsv + "\n"
                            + "File needs to have " + tab.getColumnCount()
                            + "columns separated by either of 'Tab', '|', ','", "Cannot import", JOptionPane.ERROR_MESSAGE);
                } else {
                    if (JOptionPane.showConfirmDialog(this, "About to import file\n"
                            + pathToCsv + "\n"
                            + "separator determined as " + (Character.isWhitespace(delim.charAt(0)) ? String.format("0x%02x", (int) delim.charAt(0)) : delim)
                            + "\n"
                            + "will read first " + tab.getColumnCount() + " fields"
                            + "\n\nDo you want to continue with import?", "Please confirm",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                        tab.removeAll();
                        infoTableModel.setRowCount(0);
                        parseLine(row, delim);
                        continueLoad = true;
                    }
                }
            }
            if (continueLoad) {
                while ((row = csvReader.readLine()) != null) {
                    parseLine(row, delim);
                    // do something with the data
                }
                tca.adjustColumns();
            }
        } catch (IOException ex) {
            Logger.getLogger(GridEditor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (csvReader != null) {
                try {
                    csvReader.close();
                } catch (IOException ex) {
                    Logger.getLogger(GridEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

    }

    private String guessDelimiter(String row) {
        if (StringUtils.isNotBlank(row)) {
            for (String delim : new String[]{"\t", "|", ","}) {
                String[] split = StringUtils.split(row, delim, tab.getColumnCount());
                if (ArrayUtils.isNotEmpty(split) && (mandatoryColumns > 0 && split.length >= mandatoryColumns) || split.length >= tab.getColumnCount()) {
                    return delim;
                }

            }
        }
        return null;
    }

    private void parseLine(String row, String delim) {
        if (StringUtils.isNotBlank(row)) {
            String[] split = StringUtils.split(row, delim, tab.getColumnCount());
            infoTableModel.addRow(split);
        }

    }

    public interface IAddChoices {

        public HashSet<String> getAddChoices();

    }

    public GridEditor(Window parent, String title, String _selectedFormat) {
        this(parent);
        this.selectedFormat = _selectedFormat;
        setTitle(title);

    }

    public GridEditor(Window parent) {
        super(parent);

        this.tab = new JTable();
        tca = new TableColumnAdjuster(tab);
        tca.setColumnHeaderIncluded(true);
        tab.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        theParent = parent;

        dataChanged = false;

    }

    /**
     *
     * @return true if data was changed
     */
    public boolean doShow() {

        tca.adjustColumns();
        pack();
        invalidate();
        if (logger.isTraceEnabled()) {
            logger.info("Show info PanelDialog; title=" + getTitle() + "; tab cols:" + tab.getColumnCount() + " rows: " + tab.getRowCount());
            StringBuilder s = new StringBuilder(512);
            for (int i = 0; i < tab.getRowCount(); i++) {
                s.setLength(0);
                for (int j = 0; j < tab.getColumnCount(); j++) {
                    s.append("[").append(tab.getValueAt(i, j)).append("],");
                }
                logger.trace(s);
            }

        }

        ScreenInfo.CenterWindow(this);
//        this.setLocationRelativeTo(getParent());
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

    ButtonPanel buttonPanel;
    JButton cancelButton;
    JButton addButton;
    JButton editButton;
    JButton deleteButton;
    JButton csvImportButton;

    @Override
    public ButtonPanel createButtonPanel() {
        buttonPanel = new ButtonPanel();

        addButton = new JButton();
        buttonPanel.addButton(addButton);
        addButton.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                addValuePressed(e);

            }

        });
        addButton.setText("Add");

        editButton = new JButton();
        buttonPanel.addButton(editButton);
        editButton.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editValuePressed(e);

            }

        });
        editButton.setText("Edit");

        deleteButton = new JButton();
        buttonPanel.addButton(deleteButton);
        deleteButton.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                deleteValuePressed(e);

            }

        });
        deleteButton.setText("Delete");

        csvImportButton = new JButton();
        buttonPanel.addButton(csvImportButton);

        csvImportButton.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                importCSVPressed(e);
            }

        });
        csvImportButton.setText("CSV import");

        cancelButton = new JButton();
        buttonPanel.addButton(cancelButton);

        cancelButton.setAction(new AbstractAction() {
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

    EditValuesDialog editDialog = null;

    private EditValuesDialog getEditDialog() {
        if (editDialog == null) {
            editDialog = new EditValuesDialog(this, tab.getColumnModel());
        }
        return editDialog;
    }

    private void addValuePressed(ActionEvent e) {
        ArrayList<String> vals;
        if ((vals = getEditDialog().doShow(null)) != null) {
            infoTableModel.addRow(vals.toArray(new String[vals.size()]));
            dataChanged = true;
        }

    }

    private String lastDirectory = System.getProperty("user.home");
    private JFileChooser fc = null;

    private void importCSVPressed(ActionEvent e) {

        if (fc == null) {
            fc = new JFileChooser();
            fc.setControlButtonsAreShown(true);
            fc.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.getName().toLowerCase().endsWith(".csv")) {
                        return true;
                    }
                    return false;
                }

                @Override
                public String getDescription() {
                    return "*.csv";
                }
            });
        }
        fc.setCurrentDirectory(new File(lastDirectory));
//        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//In response to a button click:
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            lastDirectory = fc.getSelectedFile().getAbsolutePath();
//            jtfOutputDir.setText(fc.getSelectedFile().getAbsolutePath());
            importFile(fc.getSelectedFile().getAbsolutePath());
        }
    }

    private ArrayList<ArrayList<String>> getAllValues() {
        ArrayList<ArrayList<String>> ret = new ArrayList<>(tab.getColumnCount());

        for (int j = 0; j < tab.getColumnCount(); j++) {
            ArrayList<String> r = new ArrayList<>(tab.getRowCount());
            for (int i = 0; i < tab.getRowCount(); i++) {
                r.add((String) tab.getValueAt(i, j));

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

    private void editValuePressed(ActionEvent e) {
        int selectedRow = tab.getSelectedRow();
        if (selectedRow >= 0) {
            ArrayList<String> vals = new ArrayList<>(tab.getColumnCount());
            for (int i = 0; i < tab.getColumnCount(); i++) {
                vals.add((String) tab.getValueAt(selectedRow, i));
            }

            if ((vals = getEditDialog().doShow(vals)) != null) {
                for (int i = 0; i < vals.size(); i++) {
                    infoTableModel.setValueAt(vals.get(i), selectedRow, i);
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
                s.append((String) tab.getValueAt(selectedRow, i));
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

    private DefaultTableModel infoTableModel;

    public void setData(Object[] columns, ArrayList<Object[]> values, int _mandatoryColumns) {
        mandatoryColumns = _mandatoryColumns;
        infoTableModel = new DefaultTableModel();
        for (Object column : columns) {
            infoTableModel.addColumn(column);
        }
        for (Object[] value : values) {
            infoTableModel.addRow(value);
        }
        tab.setModel(infoTableModel);
//        infoTableModel.fireTableDataChanged();
//        infoTableModel.fireTableStructureChanged();
    }

    public void setData(Object[] columns, ArrayList<Object[]> values) {
        setData(columns, values, columns.length);
    }

    public ArrayList<Object[]> getData() {
        ArrayList<Object[]> ret = new ArrayList<>(infoTableModel.getRowCount());
        for (int i = 0; i < infoTableModel.getRowCount(); i++) {
            Object[] vals = new Object[infoTableModel.getColumnCount()];
            for (int j = 0; j < infoTableModel.getColumnCount(); j++) {
                vals[j] = infoTableModel.getValueAt(i, j);
            }
            ret.add(vals);

        }

        return ret;
    }

    class EditValuesDialog extends StandardDialog {

        public EditValuesDialog() {
            super();
        }

        ArrayList col;
        ArrayList<EnterPanel> pan;

        private EditValuesDialog(Window parent, TableColumnModel columnModel) {
            super(parent);
            col = new ArrayList<>(columnModel.getColumnCount());
            pan = new ArrayList<>(columnModel.getColumnCount());
            for (int i = 0; i < columnModel.getColumnCount(); i++) {
                col.add(columnModel.getColumn(i).getHeaderValue());
                pan.add(new EnterPanel((String) columnModel.getColumn(i).getHeaderValue()));
            }

        }

        private ArrayList<String> doShow(ArrayList<String> vals) {
            if (vals != null) {
                setTitle("Edit entry");
                for (int i = 0; i < pan.size(); i++) {
                    EnterPanel enterPanel = pan.get(i);
                    addChoices(enterPanel, i);
                    enterPanel.setText(vals.get(i));
                }
            } else {
                for (int i = 0; i < pan.size(); i++) {
                    EnterPanel enterPanel = pan.get(i);
                    addChoices(enterPanel, i);
                    enterPanel.setText(null);
                }

                setTitle("New entry");
            }
            return doShow();
        }

        private void addChoices(EnterPanel enterPanel, int col) {
            enterPanel.clearChoices();
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

        class EnterPanel {

            public JPanel getEnterPanel() {
                return enterPanel;
            }

            private final JComboBox<String> tbValue;
            private final JPanel enterPanel;

            public String getText() {
                return (tbValue != null) ? (tbValue.getSelectedItem() != null) ? tbValue.getSelectedItem().toString() : null : null;
            }

            public void setText(String txt) {
                tbValue.setSelectedItem(txt);
            }

            EnterPanel(String title) {
                enterPanel = new JPanel();
                enterPanel.setLayout(new BoxLayout(enterPanel, BoxLayout.LINE_AXIS));
                enterPanel.add(new JLabel(title));
                tbValue = new JComboBox();
                tbValue.setEditable(true);
                enterPanel.add(tbValue);
            }

            private void clearChoices() {
                tbValue.removeAllItems();

            }

            private void addChoices(HashSet<String> addChoices) {
                for (String addChoice : addChoices) {
                    tbValue.addItem(addChoice);
                }

            }

            private void addChoices(ArrayList<String> addChoices) {
                for (String addChoice : addChoices) {
                    tbValue.addItem(addChoice);
                }

            }
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

        public ArrayList<String> doShow() {

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
                ArrayList<String> ret = new ArrayList<>(pan.size());
                for (EnterPanel enterPanel : pan) {
                    ret.add(enterPanel.getText());
                }
                return ret;

            } else {
                return null;
            }

        }
    }

    public static void main(String[] args) {
        GridEditor confServEditor = new GridEditor(null, "KVPs to add",
                "Select %d profiles");

        ArrayList<Object[]> values = new ArrayList<>();
//        for (StoredSettings.ConfServer configServer : ds.getConfigServers()) {
//            Object[] v = new Object[4];
//            v[0] = configServer.getProfile();
//            v[1] = configServer.getHost();
//            v[2] = configServer.getPort();
//            v[3] = configServer.getApp();
//            values.add(v);
//        }
        //        for (DownloadSettings.LFMTHostInstance hi : ds.getLfmtHostInstances()) {
        //            values.add(new Object[]{hi.getHost(), hi.getInstance(), hi.getBaseDir()});
        //        }

        values.add(new String[]{"1", "2", "3"});
        confServEditor.setData(new Object[]{"Section", "Key", "Value", "Action"},
                values, 3
        );
        confServEditor.doShow();
    }

}
