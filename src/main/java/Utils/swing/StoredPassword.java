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
public class StoredPassword {
    private String password;

    public String getPassword() {
        return "*******";
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public StoredPassword(String password) {
        this.password = password;
    }
}
