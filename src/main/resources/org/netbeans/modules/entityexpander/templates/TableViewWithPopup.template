package ${package};

import ${package}.ejb.${object}Facade;
import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Window;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.vaadin.maddon.fields.MTable;
import org.vaadin.maddon.label.Header;
import org.vaadin.maddon.layouts.MVerticalLayout;

@CDIView("${object?lower_case}")
public class ${object}TableViewWithPopup extends MVerticalLayout implements View {

    @Inject
    ${object}Facade cf;
    @Inject
    ${object}MaddonForm form;

    MTable<${object}> table = new MTable<>(${object}.class);

    private Window popup;

    @PostConstruct
    public void initComponent() {
        popup = new Window("Edit MicroMarket", form);
        popup.setModal(true);

        form.setResetHandler(this::reset);
        form.setSavedHandler(this::save);

        table.addMValueChangeListener(e -> {
            if (e.getValue() != null) {
                form.setEntity(e.getValue());
                getUI().addWindow(popup);
            }
        });
        listEntities();

        Button addButton = new Button("Add");
        addButton.addClickListener(e -> {
            form.setEntity(new ${object}());
            getUI().addWindow(popup);
        });

        addComponents(
                new Header("${object} listing"),
                table, 
                addButton
        );
    }

    private void listEntities() {
        table.setBeans(cf.findAll());
    }

    public void save(${object} entity) {
        if (entity.get${idfield.simpleName?cap_first}() == null) {
            cf.create(entity);
        } else {
            cf.edit(entity);
        }
        listEntities();
        Notification.show("Saved!");
    }

    public void reset(${object} entity) {
        // just hide the form
        popup.close();
        listEntities();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
    }

}
