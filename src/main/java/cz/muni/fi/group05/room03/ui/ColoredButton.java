package cz.muni.fi.group05.room03.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Graphics;
import java.util.function.Supplier;

public class ColoredButton extends JButton {

    private final static Color blue = Color.decode("#7F95A3");
    private final static Color lightBlue = Color.decode("#9CB5BE");
    private final static Color darkBlue = Color.decode("#798B91");
    private final static Color lightGreen = Color.decode("#97CFAC");
    private final static Color green = Color.decode("#83C28A");
    private final Color normal;
    private final Color hover;
    private final Color pressed;
    private boolean isActive;
    private boolean isEnabled;

    private ColoredButton(String text, Color foreground, Color normal, Color hover, Color pressed) {
        super(text);
        this.normal = normal;
        this.hover = hover;
        this.pressed = pressed;
        this.isActive = false;
        this.isEnabled = true;
        super.setContentAreaFilled(false);
        super.setForeground(foreground);
        super.setFocusPainted(false);
        super.setFont(FontsHotelUI.COLORED_BUTTON);
        super.setBorder(BorderFactory.createEmptyBorder());
    }

    public static ColoredButton large(String text) {
        return new ColoredButton(text.toUpperCase(), Color.WHITE, blue, lightBlue, lightGreen);
    }

    public static ColoredButton menu(String text) {
        return new ColoredButton(text.toUpperCase(), Color.WHITE, darkBlue, green, lightGreen);
    }

    public static ColoredButton form(String text) {
        ColoredButton button = ColoredButton.large(text);
        button.setFont(FontsHotelUI.CONFIRM_BUTTON);
        return button;
    }

    @Override
    public void setText(String text) {
        super.setText(text.toUpperCase());
    }

    public void setActive(boolean active) {
        isActive = active;
        this.repaint();
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
        this.repaint();
    }

    public void addConfirmReason(Runnable action, Supplier<String> reason) {
        super.addActionListener(actionEvent -> {
            if (!isEnabled) {
                Message.showErrorDialog(reason.get());
            } else {
                action.run();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!isEnabled) {
            g.setColor(Color.gray);
        } else if (getModel().isPressed()) {
            g.setColor(pressed);
        } else if (getModel().isRollover() || isActive) {
            g.setColor(hover);
        } else {
            g.setColor(normal);
        }
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }
}
