/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package krimtimer;

/**
 *
 * @author Enock
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.Timer;

public class StopWatchUI extends javax.swing.JFrame implements ActionListener {

    Font largeFontBOLD = new Font("Calibri", Font.BOLD, 20);
    private final Timer theChronometer;

    JPopupMenu contextMenu;
    JMenuItem fullscreenMenuItem, aboutMenuItem, exitMenuItem, countDownMenuItem,
            stopWatchMenuItem, timeUpMenuItem;

    String s;

    private int seconds = 0;
    private int minutes = 0;
    private int hour = 0;
    private long watchStart, watchEnd;
    private String pausedCntDown;
    private long pausedTime = 0;
    private boolean isChronometerRunning = false, isCountDownRunning = true, isCntDownPaused = false, paused = false;
    private DecimalFormat timeFormatter;
    private final Runnable countDown, setTime, beginTimeCount;

    ButtonGroup bg;
    PopupListener popListener;
    ImageIcon frameIcon, aboutIcon, fullScreenIcon, exitIcon;
    private ExecutorService executor = Executors.newCachedThreadPool();

    String[] min = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
        "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31",
        "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48",
        "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60"};

    String[] sec = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
        "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31",
        "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48",
        "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"};

    String[] hours = {"0", "1", "2", "3", "4", "5"};

    FullScreenTimer fullscreen;
    AboutFrame aboutFrame;
    TimeUp timeUp;

    public StopWatchUI() {

        initComponents();

        frameIcon = new ImageIcon(getClass().getResource("/images/timer.png"));
        aboutIcon = new ImageIcon(getClass().getResource("/images/info.png"));
        fullScreenIcon = new ImageIcon(getClass().getResource("/images/fullscreen.png"));
        exitIcon = new ImageIcon(getClass().getResource("/images/close.png"));

        setIconImage(frameIcon.getImage());
        setLocationRelativeTo(null);
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e); //To change body of generated methods, choose Tools | Templates.
                if (e.getKeyCode() == 70) {
                    if (!fullscreen.isVisible()) {
                        fullscreen.setVisible(true);
                    } else {
                        fullscreen.setVisible(false);
                    }
                } else if (e.getKeyCode() == 84) {
                    if (!timeUp.isVisible()) {
                        timeUp.setVisible(true);
                    }
                } else if (e.getKeyCode() == 32) {
                    if (startBtn.isEnabled()) {
                        startBtn.doClick();
                    }
                }

            }

            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == 80) {
                    pauseBtn.doClick();
                    setFocusable(true);
                } else if (e.getKeyCode() == 83) {
                    stopBtn.doClick();
                    setFocusable(true);
                } else if (e.getKeyCode() == 73) {
//                    JOptionPane.showMessageDialog(null, "This software was developed by Enock Boadi-Ansah.\n"
//                            + "For more information about the software, please write to \n"
//                            + "knocksto1@yahoo.com");
                    aboutFrame.setVisible(true);
                } else if (e.getKeyCode() == 81) {
                    System.exit(1);
                }
            }
        });

        setFocusable(true);
        stopBtn.setEnabled(false);
        startBtn.setEnabled(false);
        pauseBtn.setEnabled(false);

        bg = new ButtonGroup();
        bg.add(stopWatchRB);
        bg.add(countDownRB);

        popListener = new PopupListener();
        contextMenu = new JPopupMenu();

        fullscreenMenuItem = new JMenuItem("FullScreen                              F", fullScreenIcon);
        fullscreenMenuItem.addActionListener(this);

        timeUpMenuItem = new JMenuItem("Show Time Up Screen        T");
        timeUpMenuItem.addActionListener(this);
        stopWatchMenuItem = new JMenuItem("StopWatch");
        stopWatchMenuItem.addActionListener(this);

        countDownMenuItem = new JMenuItem("Count Down Timer");
        countDownMenuItem.addActionListener(this);

        aboutMenuItem = new JMenuItem("About                                  Ctrl+I", aboutIcon);
        aboutMenuItem.addActionListener(this);

        exitMenuItem = new JMenuItem("Exit                                      Ctrl+Q", exitIcon);
        exitMenuItem.addActionListener(this);

        contextMenu.add(fullscreenMenuItem);
        contextMenu.add(countDownMenuItem);
        contextMenu.add(stopWatchMenuItem);
        contextMenu.add(timeUpMenuItem);
        contextMenu.addSeparator();
        contextMenu.add(aboutMenuItem);
        contextMenu.add(exitMenuItem);

        addMouseListener(popListener);
        buttonPanel.addMouseListener(popListener);
        displayPanel.addMouseListener(popListener);
        this.add(contextMenu);

        displayTimerLabel.setHorizontalAlignment(JLabel.CENTER);
        startBtn.addActionListener(this);
        stopBtn.addActionListener(this);
        pauseBtn.addActionListener(this);

        countDownRB.setSelected(true);

        timeUp = new TimeUp();
        fullscreen = new FullScreenTimer();
        aboutFrame = new AboutFrame();

        timeFormatter = new DecimalFormat("00");
        hourCB.setSelectedIndex(0);
        minutesCB.setSelectedIndex(0);
        secondsCB.setSelectedIndex(0);

        if (countDownRB.isSelected()) {
            displayTimerLabel.setText(timeFormatter.format(hour) + ":"
                    + timeFormatter.format(minutes) + ":"
                    + timeFormatter.format(seconds));
            //update the fullscreen frame
            fullscreen.displayTimerLabel2.setText(timeFormatter.format(hour) + ":"
                    + timeFormatter.format(minutes) + ":"
                    + timeFormatter.format(seconds));
        }

        theChronometer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int seconds = (int) (System.currentTimeMillis() - watchStart) / 1000;
                int days = seconds / 86400;
                int hours = (seconds / 3600) - (days * 24);
                int min = (seconds / 60) - (days * 1440) - (hours * 60);
                int sec = seconds % 60;
//                s = new String("" + hours + " hours " + min + " min " + sec + " sec");
                s = String.format("%02d:%02d:%02d", hours, min, sec);
                displayTimerLabel.setText(s);
                fullscreen.displayTimerLabel2.setText(s);
            }
        });

        beginTimeCount = new Runnable() {
            @Override
            public void run() {
                while (isCountDownRunning) {
                    executor.execute(countDown);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };

        countDown = new Runnable() {
            @Override
            public void run() {

                if (seconds > 0) {
                    seconds--;
                } else {

                    if (hour == 0 && minutes == 0 && seconds == 0) {
                        isCountDownRunning = false;
                        startBtn.setEnabled(true);
                        stopWatchRB.setEnabled(true);
                        stopBtn.setEnabled(false);
                        pauseBtn.setEnabled(false);
                        fullscreen.setVisible(false);
                        hourCB.setSelectedIndex(0);
                        minutesCB.setSelectedIndex(0);
                        secondsCB.setSelectedIndex(0);
                        timeUp.setVisible(true);
                        timeUp.requestFocus();
                    } else if (minutes > 0) {

                        minutes--;
                        seconds = 59;
                        if (minutes < 5) {
                            fullscreen.displayTimerLabel2.setForeground(Color.red);
                            System.out.println("setting color condition 2");
                        }
                    } else if (hour > 0) {
                        hour--;
                        minutes = 59;
                        seconds = 59;
                    }
                }
                executor.execute(setTime);
            }
        };

        setTime = new Runnable() {
            @Override
            public void run() {
                displayTimerLabel.setText(timeFormatter.format(hour) + ":"
                        + timeFormatter.format(minutes) + ":"
                        + timeFormatter.format(seconds));

                //update the full screen frame
                fullscreen.displayTimerLabel2.setText(timeFormatter.format(hour) + ":"
                        + timeFormatter.format(minutes) + ":"
                        + timeFormatter.format(seconds));
            }
        };

    }//end of StopWatch constructor

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        displayPanel = new javax.swing.JPanel();
        minutesCB = new javax.swing.JComboBox();
        secondsCB = new javax.swing.JComboBox();
        hourCB = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        countDownRB = new javax.swing.JRadioButton();
        stopWatchRB = new javax.swing.JRadioButton();
        displayTimerLabel = new javax.swing.JLabel();
        buttonPanel = new javax.swing.JPanel();
        pauseBtn = new javax.swing.JButton();
        stopBtn = new javax.swing.JButton();
        startBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("RCCG Timer 2.2");
        setResizable(false);
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                formKeyReleased(evt);
            }
        });

        displayPanel.setBackground(new java.awt.Color(0, 0, 102));

        minutesCB.setModel(new javax.swing.DefaultComboBoxModel(min));
        minutesCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minutesCBActionPerformed(evt);
            }
        });

        secondsCB.setModel(new javax.swing.DefaultComboBoxModel(sec));
        secondsCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secondsCBActionPerformed(evt);
            }
        });

        hourCB.setModel(new javax.swing.DefaultComboBoxModel(hours));
        hourCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hourCBActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("HOURS");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("MINUTES");

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("SECONDS");

        countDownRB.setBackground(new java.awt.Color(0, 0, 102));
        countDownRB.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        countDownRB.setForeground(new java.awt.Color(255, 255, 255));
        countDownRB.setText("Count Down Timer");
        countDownRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                countDownRBActionPerformed(evt);
            }
        });

        stopWatchRB.setBackground(new java.awt.Color(0, 0, 102));
        stopWatchRB.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        stopWatchRB.setForeground(new java.awt.Color(255, 255, 255));
        stopWatchRB.setText("StopWatch");
        stopWatchRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopWatchRBActionPerformed(evt);
            }
        });

        displayTimerLabel.setBackground(new java.awt.Color(0, 0, 102));
        displayTimerLabel.setFont(new java.awt.Font("Perpetua", 1, 75)); // NOI18N
        displayTimerLabel.setForeground(new java.awt.Color(0, 153, 51));
        displayTimerLabel.setText("00:00:00");
        displayTimerLabel.setOpaque(true);

        javax.swing.GroupLayout displayPanelLayout = new javax.swing.GroupLayout(displayPanel);
        displayPanel.setLayout(displayPanelLayout);
        displayPanelLayout.setHorizontalGroup(
            displayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, displayPanelLayout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(countDownRB)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(stopWatchRB)
                .addGap(79, 79, 79))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, displayPanelLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(displayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(displayTimerLabel)
                    .addGroup(displayPanelLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(hourCB, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(minutesCB, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(jLabel3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(secondsCB, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32))
        );
        displayPanelLayout.setVerticalGroup(
            displayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(displayPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(displayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(minutesCB, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(secondsCB, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(hourCB, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2))
                .addGap(22, 22, 22)
                .addComponent(displayTimerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(displayPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(countDownRB)
                    .addComponent(stopWatchRB))
                .addContainerGap())
        );

        buttonPanel.setBackground(new java.awt.Color(255, 124, 124));

        pauseBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/player_pause.png"))); // NOI18N
        pauseBtn.setText("PAUSE");
        pauseBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseBtnActionPerformed(evt);
            }
        });

        stopBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/player_stop.png"))); // NOI18N
        stopBtn.setText("STOP");
        stopBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopBtnActionPerformed(evt);
            }
        });

        startBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/player_play.png"))); // NOI18N
        startBtn.setText("START");
        startBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(startBtn)
                .addGap(56, 56, 56)
                .addComponent(stopBtn)
                .addGap(56, 56, 56)
                .addComponent(pauseBtn)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pauseBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                    .addComponent(stopBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(startBtn, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(displayPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(5, 5, 5))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(displayPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void minutesCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minutesCBActionPerformed
        // TODO add your handling code here:
        if (countDownRB.isSelected()) {
            minutes = Integer.parseInt(minutesCB.getSelectedItem().toString());
            displayTimerLabel.setText(timeFormatter.format(hour) + ":"
                    + timeFormatter.format(minutes) + ":"
                    + timeFormatter.format(seconds));

            //update the full screen frame
            fullscreen.displayTimerLabel2.setText(timeFormatter.format(hour) + ":"
                    + timeFormatter.format(minutes) + ":"
                    + timeFormatter.format(seconds));

            if ((hourCB.getSelectedIndex() == 0) && (minutesCB.getSelectedIndex() == 0) && (secondsCB.getSelectedIndex() == 0)) {
                startBtn.setEnabled(false);
            } else {
                startBtn.setEnabled(true);
            }

        }
        requestFocus();
    }//GEN-LAST:event_minutesCBActionPerformed

    private void secondsCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secondsCBActionPerformed
        // TODO add your handling code here:
        if (countDownRB.isSelected()) {
            seconds = Integer.parseInt(secondsCB.getSelectedItem().toString());
            displayTimerLabel.setText(timeFormatter.format(hour) + ":"
                    + timeFormatter.format(minutes) + ":"
                    + timeFormatter.format(seconds));

            //update the full screen frame
            fullscreen.displayTimerLabel2.setText(timeFormatter.format(hour) + ":"
                    + timeFormatter.format(minutes) + ":"
                    + timeFormatter.format(seconds));

            if ((hourCB.getSelectedIndex() == 0) && (minutesCB.getSelectedIndex() == 0) && (secondsCB.getSelectedIndex() == 0)) {
                startBtn.setEnabled(false);
            } else {
                startBtn.setEnabled(true);
            }

        }
        this.requestFocus();
    }//GEN-LAST:event_secondsCBActionPerformed

    private void hourCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hourCBActionPerformed
        // TODO add your handling code here:
        if (countDownRB.isSelected()) {
            hour = Integer.parseInt(hourCB.getSelectedItem().toString());
            displayTimerLabel.setText(timeFormatter.format(hour) + ":"
                    + timeFormatter.format(minutes) + ":"
                    + timeFormatter.format(seconds));

            //update the full screen frame
            fullscreen.displayTimerLabel2.setText(timeFormatter.format(hour) + ":"
                    + timeFormatter.format(minutes) + ":"
                    + timeFormatter.format(seconds));

            if ((hourCB.getSelectedIndex() == 0) && (minutesCB.getSelectedIndex() == 0) && (secondsCB.getSelectedIndex() == 0)) {
                startBtn.setEnabled(false);
            } else {
                startBtn.setEnabled(true);
            }
        }
        requestFocus();
    }//GEN-LAST:event_hourCBActionPerformed

    private void countDownRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_countDownRBActionPerformed
        // TODO add your handling code here:
        if ((hourCB.getSelectedIndex() == 0) && (minutesCB.getSelectedIndex() == 0) && (secondsCB.getSelectedIndex() == 0)) {
            startBtn.setEnabled(false);
        } else {
            if (stopBtn.isEnabled()) {

            } else {
                startBtn.setEnabled(true);
            }
        }
        requestFocus();
    }//GEN-LAST:event_countDownRBActionPerformed

    private void stopWatchRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopWatchRBActionPerformed
        // TODO add your handling code here:
        if (!startBtn.isEnabled()) {
            startBtn.setEnabled(true);
        }else if (stopBtn.isEnabled()){
            JOptionPane.showMessageDialog(rootPane, "Please stop the current timer to proceed.");
        }
        requestFocus();
    }//GEN-LAST:event_stopWatchRBActionPerformed

    private void formKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyReleased
        // TODO add your handling code here:

    }//GEN-LAST:event_formKeyReleased

    private void startBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startBtnActionPerformed
        // TODO add your handling code here:
        if (hour == 0 && minutes < 5) {
            fullscreen.displayTimerLabel2.setForeground(Color.red);
        } else {
            fullscreen.displayTimerLabel2.setForeground(Color.white);
        }
        requestFocus();
    }//GEN-LAST:event_startBtnActionPerformed

    private void stopBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopBtnActionPerformed
        // TODO add your handling code here:
        hourCB.setEnabled(true);
        minutesCB.setEnabled(true);
        secondsCB.setEnabled(true);
        requestFocus();
    }//GEN-LAST:event_stopBtnActionPerformed

    private void pauseBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseBtnActionPerformed
        // TODO add your handling code here:
        requestFocus();
    }//GEN-LAST:event_pauseBtnActionPerformed
    /**
     * @param args the command line arguments
     */
//    public static void main(String args[]) {
//        /* Set the Nimbus look and feel */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
//         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(StopWatchUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(StopWatchUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(StopWatchUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(StopWatchUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /* Create and display the form */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new StopWatchUI().setVisible(true);
//            }
//        });
//    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JRadioButton countDownRB;
    private javax.swing.JPanel displayPanel;
    public javax.swing.JLabel displayTimerLabel;
    private javax.swing.JComboBox hourCB;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JComboBox minutesCB;
    private javax.swing.JButton pauseBtn;
    private javax.swing.JComboBox secondsCB;
    private javax.swing.JButton startBtn;
    private javax.swing.JButton stopBtn;
    private javax.swing.JRadioButton stopWatchRB;
    // End of variables declaration//GEN-END:variables

    @Override
    public void actionPerformed(ActionEvent e) {
        if (stopWatchRB.isSelected()) {
            if (!isChronometerRunning) {
                displayTimerLabel.setText(String.format("%02d:%02d:%02d", 0, 0, 0));
                fullscreen.displayTimerLabel2.setText(String.format("%02d:%02d:%02d", 00, 00, 00));
            }
            if (e.getActionCommand().equals("STOP")) {
                theChronometer.stop();

                displayTimerLabel.setText(String.format("%02d:%02d:%02d", 00, 00, 00));
                fullscreen.displayTimerLabel2.setText(String.format("%02d:%02d:%02d", 00, 00, 00));

                if (startBtn.getText().equalsIgnoreCase("resume")) {
                    startBtn.setText("START");
                }
                isChronometerRunning = false;
                startBtn.setEnabled(true);
                stopBtn.setEnabled(false);
                pauseBtn.setEnabled(false);
                paused = false;
                stopWatchRB.setEnabled(true);
                countDownRB.setEnabled(true);
            } // either start the Timer Thread at zero or pick up where paused.
            else if (e.getActionCommand().equals("START") || e.getActionCommand().equals("RESUME")) {
                if (!paused) {

                    pausedTime = 0;
                    System.out.println("Start clicked");
                    watchStart = System.currentTimeMillis();
                    startBtn.setEnabled(false);
                    stopBtn.setEnabled(true);
                    pauseBtn.setEnabled(true);
                    countDownRB.setEnabled(false);
                    paused = false;

                    isChronometerRunning = true;
                    theChronometer.start();
                } else {
                    System.out.println("Resume clicked");
                    watchStart = System.currentTimeMillis() + pausedTime;
                    paused = false;

                    // set the button display to Start, it may have been Resume
                    startBtn.setText("START");
                    startBtn.setEnabled(false);
                    pauseBtn.setEnabled(true);
                    stopBtn.setEnabled(true);
                    pausedTime = 0;
                    isChronometerRunning = true;
                    theChronometer.start();
                }
            } // there is no pause for Timer so we kludge one
            else if (e.getActionCommand().equals("PAUSE")) {

                System.out.println("Paused clicked");
                long now = System.currentTimeMillis();
                pausedTime -= (now - watchStart);
                theChronometer.stop();
                paused = true;

                // set the button display to Resume instead of Start
                startBtn.setText("RESUME");
                startBtn.setEnabled(true);
                pauseBtn.setEnabled(false);
            }
        } else {

            if (e.getActionCommand().equals("START") || e.getActionCommand().equals("RESUME")) {
                if (!isCountDownRunning) {
                    isCountDownRunning = true;
                }
                if (isCntDownPaused) {
                    isCntDownPaused = false;
                    executor.notify();

                }
                startBtn.setEnabled(false);
                stopBtn.setEnabled(true);
                pauseBtn.setEnabled(true);
                hourCB.setEnabled(false);
                minutesCB.setEnabled(false);
                secondsCB.setEnabled(false);
                if (countDownRB.isSelected()) {
                    stopWatchRB.setEnabled(false);
                } else {
                    countDownRB.setEnabled(false);

                }
                executor.execute(beginTimeCount);
            } else if (e.getActionCommand().equals("PAUSE")) {

                pausedCntDown = displayTimerLabel.getText();
                try {
                    executor.wait();
                } catch (Exception ex) {

                }
                isCountDownRunning = false;

                displayTimerLabel.setText(pausedCntDown);
                startBtn.setText("RESUME");
                startBtn.setEnabled(true);
                pauseBtn.setEnabled(false);
            } else if (e.getActionCommand().equals("STOP")) {
                isCountDownRunning = false;

                hour = 0;
                seconds = 0;
                minutes = 0;
                displayTimerLabel.setText(timeFormatter.format(hour) + ":"
                        + timeFormatter.format(minutes) + ":"
                        + timeFormatter.format(seconds));

                //update the full screen frame
                fullscreen.displayTimerLabel2.setText(timeFormatter.format(hour) + ":"
                        + timeFormatter.format(minutes) + ":"
                        + timeFormatter.format(seconds));

                hourCB.setSelectedIndex(0);
                minutesCB.setSelectedIndex(0);
                secondsCB.setSelectedIndex(0);

                if (startBtn.getText().equalsIgnoreCase("resume")) {
                    startBtn.setText("START");
                }
                startBtn.setEnabled(false);
                stopBtn.setEnabled(false);
                pauseBtn.setEnabled(false);
                stopWatchRB.setEnabled(true);
                countDownRB.setEnabled(true);
                hourCB.setEnabled(false);
                minutesCB.setEnabled(false);
                secondsCB.setEnabled(false);
                requestFocus();

            }

        }

        if (e.getSource() == fullscreenMenuItem) {
            fullscreen.setVisible(true);
        } else if (e.getSource() == timeUpMenuItem) {
            timeUp.setVisible(true);
        } else if (e.getSource() == stopWatchMenuItem) {
            if (startBtn.isEnabled()) {
                stopWatchRB.setSelected(true);
            }
        } else if (e.getSource() == countDownMenuItem) {
            if (startBtn.isEnabled()) {
                countDownRB.setSelected(true);
            }
        } else if (e.getSource() == aboutMenuItem) {
//            JOptionPane.showMessageDialog(null, "This software was developed by Enock Boadi-Ansah.\n"
//                    + "For more information about the software, please write to \n"
//                    + "knocksto1@yahoo.com");
            aboutFrame.setVisible(true);
        } else if (e.getSource() == exitMenuItem) {
            int option = JOptionPane.showConfirmDialog(null, "Are you sure you want exit application",
                    "Exit Application", JOptionPane.YES_NO_OPTION);
            if (option == 0) {
                System.exit(1);
            }
        }

    }

    class PopupListener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }

        private void showPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                contextMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

}
