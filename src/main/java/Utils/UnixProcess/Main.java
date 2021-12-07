/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Singleton.java to edit this template
 */
package Utils.UnixProcess;

/**
 *
 * @author stisy7
 */
public class Main {
    
    private Main() {
    }
    
    public static Main getInstance() {
        return MainHolder.INSTANCE;
    }
    
    private static class MainHolder {

        private static final Main INSTANCE = new Main();
    }
}
