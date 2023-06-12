module ProjektMAS {
    requires java.naming;
    requires lombok;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires javafx.graphics;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires reflections;
    requires org.jetbrains.annotations;

    opens mas;
    opens mas.entity;
    opens mas.gui.controllers;
    exports mas;
    exports mas.entity;
}