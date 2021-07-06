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
public class FieldProfile {

    private final String name;
    private final EditType editType;
    private final Class<? extends EditableValue> newObjType;

    public Class<? extends EditableValue> getNewObjType() {
        return newObjType;
    }

    public String getName() {
        return name;
    }

    public EditType getEditType() {
        return editType;
    }

    public FieldProfile(String name, EditType editType, Class<? extends EditableValue> newObjType) {
        this.name = name;
        this.editType = editType;
        this.newObjType = newObjType;
    }

}
