/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import static Utils.UTCTimeRange.getUtcTime;
import static Utils.UTCTimeRange.zoneId;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.components.DateTimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;
import com.github.lgooddatepicker.optionalusertools.PickerUtilities;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.Instant;
import java.time.ZonedDateTime;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

/**
 *
 * @author Stepan
 */
public class TDateRange extends javax.swing.JPanel {

    public static void main(String[] args) {
        // Trying to set Nimbus look and feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
            System.out.println("Failed to apply Nimbus look and feel");

        }
        TDateRange tDateRange = new TDateRange();
        JFrame jf = new JFrame();
        jf.setLayout(new FlowLayout());
        jf.setSize(new Dimension(640, 480));
        jf.add(tDateRange);
        jf.setVisible(true);
        jf.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public static long getUTCTime(DateTimePicker dtp, int adjustment) {
        return getUtcTime(dtp.getDateTimePermissive(), zoneId, adjustment);
    }

    public static void setTimeRange(DateTimePicker dtp, long time) {
        ZonedDateTime zoneDateTime = (Instant.ofEpochMilli(time)).atZone(zoneId);

        dtp.setDateTimePermissive(zoneDateTime.toLocalDateTime());
        DatePickerSettings dateSettings = dtp.getDatePicker().getSettings();
//        dateSettings.setDateRangeLimits(zoneDateTime.toLocalDate(), zoneDateTime.toLocalDate());

//            long toEpochDay = dtFrom.getDatePicker().getDate().atTime(LocalTime.MIN)
//            inquirer.inquirer.logger.info("getTimeRange toEpochDay " + toEpochDay);
//            long toNanoOfDay = dtFrom.getTimePicker().getTime().toNanoOfDay();
//            inquirer.inquirer.logger.info("getTimeRange toNanoOfDay " + toEpochDay +"total: "+toEpochDay*1000000+toNanoOfDay);
//            instantFrom = dtLocalFrom.toInstant(ZoneOffset.UTC);
//            inquirer.inquirer.logger.info("instant: "+instantFrom+" getTimeRange " + instantFrom.getEpochSecond() + " to " + instantFrom.getNano());
    }

    private IRefresh refreshCB = null;

    private JButton refreshBt;

    /**
     * Creates new form TDateRange
     */
    JLabel jlFrom;
    JLabel jlTo;
    private DateTimePicker dtFrom;
    private DateTimePicker dtTo;

    public TDateRange() {
        this(true);
    }

    public TDateRange(boolean showSeconds) {
        super();
        initComponents();
        initDates(showSeconds);

    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled); //To change body of generated methods, choose Tools | Templates.
        if (refreshBt != null) {
            refreshBt.setEnabled(enabled);
        }
        dtFrom.setEnabled(enabled);
        dtTo.setEnabled(enabled);
        jlFrom.setEnabled(enabled);
        jlTo.setEnabled(enabled);
    }

    public void enableFrom(boolean enabled) {
        dtFrom.setEnabled(enabled);
        jlFrom.setEnabled(enabled);
    }

    public void enableTo(boolean enabled) {
        dtTo.setEnabled(enabled);
        jlTo.setEnabled(enabled);
    }

    public IRefresh getRefreshCB() {
        return refreshCB;
    }

    public void setRefreshCB(IRefresh refreshCB) {
        this.refreshCB = refreshCB;
        refreshBt = new JButton("refresh");
        add(refreshBt);

        refreshBt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IRefresh refreshCB1 = getRefreshCB();
                if (refreshCB1 != null) {
                    setTimeRange(refreshCB1.Refresh());
                }
            }
        });
    }

    private void initDates(boolean showSeconds) {
        String dateFormat = (showSeconds) ? "HH:mm:ss" : "HH:mm";
//        setLayout(new FlowLayout());
        JPanel pDates = new JPanel();
        pDates.setLayout(new BoxLayout(pDates, BoxLayout.LINE_AXIS));

        jlFrom = new JLabel("From");
        pDates.add(jlFrom);
        dtFrom = newPicker(dateFormat);
        pDates.add(dtFrom);
        jlTo = new JLabel("To");
        pDates.add(jlTo);
        dtTo = newPicker(dateFormat);
        pDates.add(dtTo);

        dtFrom.getTimePicker().getSettings().setDisplayToggleTimeMenuButton(true);
        dtTo.getTimePicker().getSettings().setDisplayToggleTimeMenuButton(true);
        pDates.validate();
        pDates.setMaximumSize(new Dimension(pDates.getMinimumSize().width, pDates.getMinimumSize().height));
        add(pDates);
    }

    private DateTimePicker newPicker() {
        return newPicker("HH:mm:ss");
    }

    private DateTimePicker newPicker(String dateFormat) {
        DateTimePicker dateTimePicker1 = new DateTimePicker();
//        dateTimePicker1.datePicker.setDate(LocalDate.now());
//        dateTimePicker1.timePicker.setTimeToNow();
        dateTimePicker1.getDatePicker().setBorder(null);
        TimePickerSettings timeSettings = dateTimePicker1.getTimePicker().getSettings();
        timeSettings.setFormatForDisplayTime(PickerUtilities.createFormatterFromPatternString(
                dateFormat, timeSettings.getLocale()));
        timeSettings.setInitialTimeToNow();
        timeSettings.setFormatForMenuTimes(PickerUtilities.createFormatterFromPatternString(
                dateFormat, timeSettings.getLocale()));
        timeSettings.setInitialTimeToNow();
        return dateTimePicker1;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));
    }// </editor-fold>//GEN-END:initComponents

    public UTCTimeRange getTimeRange() {
        if (dtFrom.isEnabled() && dtTo.isEnabled()) {
            UTCTimeRange range = new UTCTimeRange();

            range.setStart(getUtcTime(dtFrom.getDateTimePermissive(), zoneId, 0));
            range.setEnd(getUtcTime(dtTo.getDateTimePermissive(), zoneId, 1));
//        inquirer.inquirer.logger.info("setTimeRange " + range.get(0) + " to " + range.get(1));
            return range;
        }
        return null;
    }

    public UTCTimeRange getTimeRangeAlways() {
        UTCTimeRange range = new UTCTimeRange();

        if (dtFrom.isEnabled()) {
            range.setStart(getUtcTime(dtFrom.getDateTimePermissive(), zoneId, 0));
        } else {
            range.setStart((long) 0);
        }
        if (dtTo.isEnabled()) {
            range.setEnd(getUtcTime(dtTo.getDateTimePermissive(), zoneId, 1));
        } else {
            range.setEnd((long) 0);
        }
//        inquirer.inquirer.logger.info("setTimeRange " + range.get(0) + " to " + range.get(1));
        return range;
    }

    public void setTimeRange(UTCTimeRange timeRange) {
        dtFrom.setEnabled(!(timeRange == null));
        dtTo.setEnabled(!(timeRange == null));

        if (timeRange == null) {
            dtFrom.getDatePicker().clear();
            dtFrom.getTimePicker().clear();
            dtTo.getDatePicker().clear();
            dtTo.getTimePicker().clear();
        } else {
            setTimeRange(dtFrom, timeRange.getStart());
            setTimeRange(dtTo, timeRange.getEnd());

        }
    }

    public static interface IRefresh {

        UTCTimeRange Refresh();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
