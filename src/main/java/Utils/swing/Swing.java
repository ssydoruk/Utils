/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils.swing;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author stepan_sydoruk
 */
public class Swing {

    public static void setChoices(JComboBox<String> box, Collection<String> choices) {
        DefaultComboBoxModel model = (DefaultComboBoxModel) box.getModel();
        model.removeAllElements();
        for (String choice : choices) {
            model.addElement(choice);
        }
//        if (model.getSize() > 0) {
        box.setSelectedIndex(-1);
//        }
    }

    public static String checkBoxSelection(JComboBox<String> tfSection) {
        return (tfSection.getSelectedItem() != null) ? StringUtils.stripToNull(tfSection.getSelectedItem().toString()) : null;
    }

    public static Collection<String> getChoices(JComboBox<String>... boxes) {
        Collection<String> ret = new ArrayList();
        for (JComboBox<String> jComboBox : boxes) {
            String s = (String) jComboBox.getSelectedItem();
            if (StringUtils.isNotBlank(s)) {
                ret.add(s);
            }
        }
        if (ret.isEmpty()) {
            return null;
        } else {
            return ret;
        }

    }

    public static void restrictHeight(JComponent comp) {
        comp.setMaximumSize(new Dimension(comp.getMaximumSize().width, comp.getMinimumSize().height));

    }

}
