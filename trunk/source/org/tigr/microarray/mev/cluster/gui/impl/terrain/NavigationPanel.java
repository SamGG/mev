/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: NavigationPanel.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-03-24 15:52:04 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.terrain;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.tigr.microarray.mev.cluster.gui.impl.GUIFactory;

public class NavigationPanel extends JPanel implements ChangeListener, ActionListener {

    private static final int FWD_CMD   = 0;
    private static final int BWD_CMD   = 1;
    private static final int UP_CMD    = 2;
    private static final int DOWN_CMD  = 3;
    private static final int LEFT_CMD  = 4;
    private static final int RIGHT_CMD = 5;

    private AbstractButton lu_btn;
    private AbstractButton lm_btn;
    private AbstractButton lb_btn;

    private AbstractButton mu_btn;
    private AbstractButton mm_btn;
    private AbstractButton mb_btn;

    private AbstractButton ru_btn;
    private AbstractButton rm_btn;
    private AbstractButton rb_btn;

    private TimerHandler timerHandler;
    private KeyMotionBehavior behavior;

    private static final int MOVE_MODE = 0;
    private static final int TURN_MODE = 1;
    private static final int ROT_MODE  = 2;

    private static Icon[] mode_icons = new Icon[3];
    private int state = MOVE_MODE;

    static {
        mode_icons[MOVE_MODE] = GUIFactory.getIcon("move_mode.gif");
        mode_icons[TURN_MODE] = GUIFactory.getIcon("turn_mode.gif");
        mode_icons[ROT_MODE ] = GUIFactory.getIcon("rot_mode.gif");
    }

    public NavigationPanel(){}
    public NavigationPanel(KeyMotionBehavior behavior) {
        setLayout(new GridBagLayout());
        this.behavior = behavior;
        this.timerHandler = new TimerHandler();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0; gbc.gridy = 0;
        add((lu_btn = createJButton("left-up-command", GUIFactory.getIcon("arrow_left_up.gif"))), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        add((mu_btn = createJButton("middle-up-command", GUIFactory.getIcon("arrow_up.gif"))), gbc);
        gbc.gridx = 2; gbc.gridy = 0;
        add((ru_btn = createJButton("right-up-command", GUIFactory.getIcon("arrow_right_up.gif"))), gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        add((lm_btn = createJButton("left-middle-command", GUIFactory.getIcon("arrow_left.gif"))), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        add((mm_btn = createStateButton("middle-middle-command", mode_icons[MOVE_MODE])), gbc);
        gbc.gridx = 2; gbc.gridy = 1;
        add((rm_btn = createJButton("right-middle-command", GUIFactory.getIcon("arrow_right.gif"))), gbc);
        gbc.gridx = 0; gbc.gridy = 2;
        add((lb_btn = createJButton("left-bottom-command", GUIFactory.getIcon("arrow_left_down.gif"))), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        add((mb_btn = createJButton("middle-bottom-command", GUIFactory.getIcon("arrow_down.gif"))), gbc);
        gbc.gridx = 2; gbc.gridy = 2;
        add((rb_btn = createJButton("right-bottom-command", GUIFactory.getIcon("arrow_right_down.gif"))), gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.gridheight = 3;
        JButton reset = new JButton("<html><body><table cellspacing=0 cellpadding=0><tr><td>R</td></tr><tr><td>E</td></tr><tr><td>S</td></tr><tr><td>E</td></tr><tr><td>T</td></tr></table></body></html>");
        reset.setActionCommand("reset-command");
        reset.addActionListener(this);
        add(reset, gbc);
        setTooltips(this.state);
    }

    protected AbstractButton createStateButton(String command, Icon icon) {
        JButton button = new JButton(icon);
        configureButton(button);
        button.setActionCommand(command);
        button.addActionListener(this);
        return button;
    }

    protected AbstractButton createJButton(String command, Icon icon) {
        JButton button = new JButton(icon);
        configureButton(button);
        button.setActionCommand(command);
        button.addChangeListener(this);
        return button;
    }

    protected void configureButton(AbstractButton button) {
        button.setContentAreaFilled(false);
        button.setMargin(new Insets(0,0,0,0));
        button.setFocusPainted(false);
    }

    public void stateChanged(ChangeEvent e) {
        AbstractButton button = (AbstractButton)e.getSource();
        ButtonModel model = button.getModel();
        if (model.isPressed() && model.isArmed())
            this.timerHandler.start(button.getActionCommand());
        else
            this.timerHandler.stop();
    }

    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (command.equals("middle-middle-command")) {
            if (this.state == ROT_MODE)
                this.state = MOVE_MODE-1;
            this.state++;
            mm_btn.setIcon(mode_icons[this.state]);
            setTooltips(this.state);
        } else if (command.equals("reset-command")) {
            this.behavior.execute(KeyMotionBehavior.RESET_CMD);
        } else if (command.equals("left-up-command")) {
            this.behavior.execute(getMotionCommand(FWD_CMD));
        } else if (command.equals("middle-up-command")) {
            this.behavior.execute(getMotionCommand(UP_CMD));
        } else if (command.equals("right-up-command")) {
            this.behavior.execute(getMotionCommand(FWD_CMD));
        } else if (command.equals("left-middle-command")) {
            this.behavior.execute(getMotionCommand(LEFT_CMD));
        } else if (command.equals("right-middle-command")) {
            this.behavior.execute(getMotionCommand(RIGHT_CMD));
        } else if (command.equals("left-bottom-command")) {
            this.behavior.execute(getMotionCommand(BWD_CMD));
        } else if (command.equals("middle-bottom-command")) {
            this.behavior.execute(getMotionCommand(DOWN_CMD));
        } else if (command.equals("right-bottom-command")) {
            this.behavior.execute(getMotionCommand(BWD_CMD));
        }
    }

    protected int getMotionCommand(int code) {
        switch (this.state) {
        case MOVE_MODE: 
            switch (code) {
            case FWD_CMD:
                return KeyMotionBehavior.MOVE_FWD_CMD;
            case BWD_CMD:
                return KeyMotionBehavior.MOVE_BWD_CMD;
            case UP_CMD:
                return KeyMotionBehavior.MOVE_UP_CMD;
            case DOWN_CMD:
                return KeyMotionBehavior.MOVE_DOWN_CMD;
            case LEFT_CMD:
                return KeyMotionBehavior.MOVE_LEFT_CMD;
            case RIGHT_CMD:
                return KeyMotionBehavior.MOVE_RIGHT_CMD;
            }
            break;
        case TURN_MODE:
            switch (code) {
            case FWD_CMD:
                return KeyMotionBehavior.TILT_RIGHT_CMD;
            case BWD_CMD:
                return KeyMotionBehavior.TILT_LEFT_CMD;
            case UP_CMD:
                return KeyMotionBehavior.TURN_UP_CMD;
            case DOWN_CMD:
                return KeyMotionBehavior.TURN_DOWN_CMD;
            case LEFT_CMD:
                return KeyMotionBehavior.TURN_LEFT_CMD;
            case RIGHT_CMD:
                return KeyMotionBehavior.TURN_RIGHT_CMD;
            }
            break;
        case ROT_MODE:
            switch (code) {
            case FWD_CMD:
                return KeyMotionBehavior.ROT_Z_POS_CMD;
            case BWD_CMD:
                return KeyMotionBehavior.ROT_Z_NEG_CMD;
            case UP_CMD:
                return KeyMotionBehavior.ROT_X_POS_CMD;
            case DOWN_CMD:
                return KeyMotionBehavior.ROT_X_NEG_CMD;
            case LEFT_CMD:
                return KeyMotionBehavior.ROT_Y_NEG_CMD;
            case RIGHT_CMD:
                return KeyMotionBehavior.ROT_Y_POS_CMD;
            }
            break;
        }
        return -1;
    }

    protected void setTooltips(int mode) {
        switch (mode) {
        case MOVE_MODE:
            this.lu_btn.setToolTipText("move view forward");
            this.lm_btn.setToolTipText("move view left");
            this.lb_btn.setToolTipText("move view backward");
            this.mu_btn.setToolTipText("move view up");
            this.mm_btn.setToolTipText("switch to turn mode");
            this.mb_btn.setToolTipText("move view down");
            this.ru_btn.setToolTipText("move view forward");
            this.rm_btn.setToolTipText("move view right");
            this.rb_btn.setToolTipText("move view backward");
            break;
        case TURN_MODE:
            this.lu_btn.setToolTipText("tilt view right");
            this.lm_btn.setToolTipText("turn view left");
            this.lb_btn.setToolTipText("tilt view left");
            this.mu_btn.setToolTipText("turn view up");
            this.mm_btn.setToolTipText("switch to rotation mode");
            this.mb_btn.setToolTipText("turn view down");
            this.ru_btn.setToolTipText("tilt view right");
            this.rm_btn.setToolTipText("turn view right");
            this.rb_btn.setToolTipText("tilt view left");
            break;
        case ROT_MODE:
            this.lu_btn.setToolTipText("rotate z");
            this.lm_btn.setToolTipText("rotate y");
            this.lb_btn.setToolTipText("rotate z");
            this.mu_btn.setToolTipText("rotate x");
            this.mm_btn.setToolTipText("switch to motion mode");
            this.mb_btn.setToolTipText("rotate x");
            this.ru_btn.setToolTipText("rotate z");
            this.rm_btn.setToolTipText("rotate y");
            this.rb_btn.setToolTipText("rotate z");
            break;
        }
    }

    private class TimerHandler implements ActionListener {
        private Timer timer;
        private String command;

        public TimerHandler() {
            this.timer = new Timer(100, this);
        }

        public void start(String command) {
            this.command = command;
            actionPerformed(null);
            this.timer.start();
        }

        public void stop() {
            this.timer.stop();
        }

        public void actionPerformed(ActionEvent event) {
            NavigationPanel.this.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, this.command));
        }
    }

}
