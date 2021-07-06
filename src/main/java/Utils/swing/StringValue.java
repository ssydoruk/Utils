/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils.swing;

/**
 *
 * @author stepan_sydoruk
 */
public class StringValue extends EditableValue<String> {

    @Override
    public String toString() {
        return getValue();
    }

    public StringValue(String val) {
        super.setValue(val);
    }

    public StringValue() {
    }
    
}
