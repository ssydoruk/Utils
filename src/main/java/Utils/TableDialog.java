/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.awt.HeadlessException;
import java.awt.Window;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author stepan_sydoruk
 */
public class TableDialog extends Utils.InfoPanel {

    TableColumnAdjuster tca;
    JTable tab;

    public TableDialog(Window parent, int buttonOptions) throws HeadlessException {
        super(parent, buttonOptions);

//        infoTableModel.addColumn("LDAP");
        tab = new JTable();
        tab.getTableHeader().setVisible(true);
        tab.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane jp = new JScrollPane(tab);
        super.setMainPanel(jp);

        tca = new TableColumnAdjuster(tab);
        tca.setColumnDataIncluded(true);
        tca.setColumnHeaderIncluded(false);
        tca.setDynamicAdjustment(true);
    }

    public boolean shouldProceed(final String[] columnTitles, final ArrayList<String[]> tableRows,
            String title) {
        DefaultTableModel infoTableModel;
        infoTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // To change body of generated methods, choose Tools | Templates.
            }

        };

        for (String string : columnTitles) {
            infoTableModel.addColumn(string);
        }

        for (String[] entry : tableRows) {
            infoTableModel.addRow(entry);
        }
        tab.setModel(infoTableModel);
        tca.adjustColumns();
        this.setTitle(title);
        Utils.ScreenInfo.CenterWindow(this);
        this.showModal();

        return getDialogResult() == JOptionPane.OK_OPTION;
    }

}
