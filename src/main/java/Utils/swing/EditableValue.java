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
public abstract class EditableValue<T> {
    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    
    @Override
    public  abstract String toString();
    
    
}
