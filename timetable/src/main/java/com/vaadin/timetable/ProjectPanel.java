package com.vaadin.timetable;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

public class ProjectPanel extends VerticalLayout {

    public ProjectPanel(String postedDate, String facultyCode, String title, String desc, String batch, int marks, String dueDate, String dueTime) {
        Accordion panel = new Accordion();

        addClassName("project-panel");

        Label posted = new Label("Posted on");
        posted.addClassName("bolden");
        Label description = new Label(desc);
        panel.add(title,new VerticalLayout(new HorizontalLayout(posted,new Label(postedDate)),description));
        panel.close();
        add(panel);
    }
}