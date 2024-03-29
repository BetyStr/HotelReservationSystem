package cz.muni.fi.group05.room03.ui.form;

import cz.muni.fi.group05.room03.ui.FontsHotelUI;
import cz.muni.fi.group05.room03.ui.form.field.Field;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.PAGE_START;

public class Form {

    private final FormBuilder builder;

    private Form(FormBuilder builder) {
        this.builder = builder;
    }

    public JPanel getPanel() {
        return builder.panel;
    }

    public void addResetFields(List<Field<?>> fields) {
        builder.panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                fields.forEach(Field::reset);
                fields.forEach(f -> f.setEnabled(true));
            }
        });
    }

    public static class FormBuilder {

        private final JPanel panel = new JPanel(new GridBagLayout());
        private final GridBagConstraints constraints = new GridBagConstraints();

        public FormBuilder(String formName) {
            constraints.fill = HORIZONTAL;
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.weightx = 1.0;
            constraints.anchor = PAGE_START;
            constraints.insets = new Insets(5, 15, 5, 0);
            constraints.gridwidth = 4;

            JLabel label = new JLabel(formName.toUpperCase(), SwingConstants.LEFT);
            label.setFont(FontsHotelUI.TITLE);
            panel.add(label, constraints);
            constraints.gridy++;

            JSeparator separator = new JSeparator();
            separator.setOrientation(SwingConstants.HORIZONTAL);
            constraints.insets = new Insets(0, 0, 0, 0);
            panel.add(separator, constraints);
            constraints.gridwidth = 2;
            constraints.gridy++;
            constraints.insets = new Insets(20, 10,0 ,10);
        }

        public FormBuilder addComponent(Field<?> component, String label) {
            constraints.gridx = 0;
            JLabel jLabel = new JLabel(label.toUpperCase());
            jLabel.setFont(FontsHotelUI.FORM);
            component.getComponent().setFont(FontsHotelUI.FORM);
            panel.add(jLabel, constraints);
            constraints.gridx = 2;
            panel.add(component.getComponent(), constraints);
            constraints.gridy++;
            return this;
        }

        public Form build(JButton confirm, JButton cancel){
            constraints.insets = new Insets(10, 10, 10, 10);
            constraints.gridwidth = 1;
            constraints.weighty = 1;
            constraints.anchor = GridBagConstraints.PAGE_END;
            constraints.gridx = 2;
            constraints.gridy++;
            panel.add(cancel, constraints);

            constraints.insets = new Insets(10, 10, 10, 10);
            constraints.gridwidth = 1;
            constraints.gridx = 3;
            panel.add(confirm, constraints);

            return new Form(this);
        }
    }
}
