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
public class PasswordValue extends EditableValue<String> {

    @Override
    public String toString() {
        return "***";
    }

    public PasswordValue(String pwd) {
        super.setValue(pwd);
    }

    public PasswordValue() {
    }
    
}
