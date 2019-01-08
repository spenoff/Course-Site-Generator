package csg.workspace.controllers;

import djf.modules.AppGUIModule;
import djf.ui.dialogs.AppDialogsFacade;
import javafx.collections.ObservableList;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import csg.CourseSiteGeneratorApp;

import static csg.CourseSiteGeneratorOfficeHoursPropertyType.CSG_EMAIL_TEXT_FIELD;
import static csg.CourseSiteGeneratorOfficeHoursPropertyType.CSG_END_TIME_COMBO_BOX;
import static csg.CourseSiteGeneratorOfficeHoursPropertyType.CSG_FOOLPROOF_SETTINGS;
import static csg.CourseSiteGeneratorOfficeHoursPropertyType.CSG_NAME_TEXT_FIELD;
import static csg.CourseSiteGeneratorOfficeHoursPropertyType.CSG_NO_TA_SELECTED_CONTENT;
import static csg.CourseSiteGeneratorOfficeHoursPropertyType.CSG_NO_TA_SELECTED_TITLE;
import static csg.CourseSiteGeneratorOfficeHoursPropertyType.CSG_OFFICE_HOURS_TABLE_VIEW;
import static csg.CourseSiteGeneratorOfficeHoursPropertyType.CSG_START_TIME_COMBO_BOX;
import static csg.CourseSiteGeneratorOfficeHoursPropertyType.CSG_TAS_TABLE_VIEW;
import static csg.CourseSiteGeneratorOfficeHoursPropertyType.CSG_TA_EDIT_DIALOG;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_EXPORT_DIRECTORY;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_NUMBER_COMBO_BOX;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_SEMESTER_COMBO_BOX;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_SUBJECT_COMBO_BOX;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_YEAR_COMBO_BOX;
import csg.data.CourseSiteGeneratorData;
import csg.data.TAType;
import csg.data.TeachingAssistantPrototype;
import csg.data.TimeSlot;
import csg.data.TimeSlot.DayOfWeek;
import csg.transactions.AddTA_Transaction;
import csg.transactions.EditTA_Transaction;
import csg.transactions.ToggleOfficeHours_Transaction;
import csg.workspace.dialogs.TADialog;
import static csg.workspace.style.CSGStyle.CLASS_CSG_PANE;
import static djf.modules.AppGUIModule.ENABLED;
import djf.ui.AppNodesBuilder;
import java.util.ArrayList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 *
 * @author McKillaGorilla
 */
public class CourseSiteGeneratorController {

    CourseSiteGeneratorApp app;

    public CourseSiteGeneratorController(CourseSiteGeneratorApp initApp) {
        app = initApp;
    }
    
    public void processUpdateExportDirectory() {
        AppGUIModule gui = app.getGUIModule();
        String subject = (String)((ComboBox)gui.getGUINode(CSG_SUBJECT_COMBO_BOX)).getSelectionModel().getSelectedItem();
        String number = (String)((ComboBox)gui.getGUINode(CSG_NUMBER_COMBO_BOX)).getSelectionModel().getSelectedItem();
        String semester = (String)((ComboBox)gui.getGUINode(CSG_SEMESTER_COMBO_BOX)).getSelectionModel().getSelectedItem();
        int year = (int)((ComboBox)gui.getGUINode(CSG_YEAR_COMBO_BOX)).getSelectionModel().getSelectedItem();
        Label expText = (Label)gui.getGUINode(CSG_EXPORT_DIRECTORY);
        expText.setText(".\\export\\" + subject + "_" + number + "_" + semester + "_" + year + "\\public_html");
    }

    public void processAddTA() {
        AppGUIModule gui = app.getGUIModule();
        TextField nameTF = (TextField) gui.getGUINode(CSG_NAME_TEXT_FIELD);
        String name = nameTF.getText();
        TextField emailTF = (TextField) gui.getGUINode(CSG_EMAIL_TEXT_FIELD);
        String email = emailTF.getText();
        CourseSiteGeneratorData data = (CourseSiteGeneratorData) app.getDataComponent();
        TAType type = data.getSelectedType();
        if (data.isLegalNewTA(name, email)) {
            TeachingAssistantPrototype ta = new TeachingAssistantPrototype(name.trim(), email.trim(), type);
            AddTA_Transaction addTATransaction = new AddTA_Transaction(data, ta);
            app.processTransaction(addTATransaction);

            // NOW CLEAR THE TEXT FIELDS
            nameTF.setText("");
            emailTF.setText("");
            nameTF.requestFocus();
        }
        app.getFoolproofModule().updateControls(CSG_FOOLPROOF_SETTINGS);
    }

    public void processVerifyTA() {

    }

    public void processToggleOfficeHours() {
        AppGUIModule gui = app.getGUIModule();
        TableView<TimeSlot> officeHoursTableView = (TableView) gui.getGUINode(CSG_OFFICE_HOURS_TABLE_VIEW);
        ObservableList<TablePosition> selectedCells = officeHoursTableView.getSelectionModel().getSelectedCells();
        //System.out.println(selectedCells.get)
        if (selectedCells.size() > 0) {
            TablePosition cell = selectedCells.get(0);
            int cellColumnNumber = cell.getColumn();
            CourseSiteGeneratorData data = (CourseSiteGeneratorData)app.getDataComponent();
            if (data.isDayOfWeekColumn(cellColumnNumber)) {
                DayOfWeek dow = data.getColumnDayOfWeek(cellColumnNumber);
                TableView<TeachingAssistantPrototype> taTableView = (TableView)gui.getGUINode(CSG_TAS_TABLE_VIEW);
                TeachingAssistantPrototype ta = taTableView.getSelectionModel().getSelectedItem();
                if (ta != null) {
                    TimeSlot timeSlot = officeHoursTableView.getSelectionModel().getSelectedItem();
                    ToggleOfficeHours_Transaction transaction = new ToggleOfficeHours_Transaction(data, timeSlot, dow, ta);
                    app.processTransaction(transaction);
                }
                else {
                    Stage window = app.getGUIModule().getWindow();
                    AppDialogsFacade.showMessageDialog(window, CSG_NO_TA_SELECTED_TITLE, CSG_NO_TA_SELECTED_CONTENT);
                }
            }
            int row = cell.getRow();
            cell.getTableView().refresh();
        }
        System.out.println(officeHoursTableView.getSelectionModel().cellSelectionEnabledProperty().getValue());
        System.out.println(officeHoursTableView.getSelectionModel().getSelectionMode());
    }

    public void processTypeTA() {
        app.getFoolproofModule().updateControls(CSG_FOOLPROOF_SETTINGS);
    }
    

    public void processEditTA() {
        CourseSiteGeneratorData data = (CourseSiteGeneratorData)app.getDataComponent();
        if (data.isTASelected()) {
            TeachingAssistantPrototype taToEdit = data.getSelectedTA();
            TADialog taDialog = (TADialog)app.getGUIModule().getDialog(CSG_TA_EDIT_DIALOG);
            taDialog.showEditDialog(taToEdit);
            TeachingAssistantPrototype editTA = taDialog.getEditTA();
            if (editTA != null) {
                EditTA_Transaction transaction = new EditTA_Transaction(taToEdit, editTA.getName(), editTA.getEmail(), editTA.getType());
                app.processTransaction(transaction);
            }
        }
    }

    public void processSelectAllTAs() {
        CourseSiteGeneratorData data = (CourseSiteGeneratorData)app.getDataComponent();
        data.selectTAs(TAType.All);
    }

    public void processSelectGradTAs() {
        CourseSiteGeneratorData data = (CourseSiteGeneratorData)app.getDataComponent();
        data.selectTAs(TAType.Graduate);
    }

    public void processSelectUndergradTAs() {
        CourseSiteGeneratorData data = (CourseSiteGeneratorData)app.getDataComponent();
        data.selectTAs(TAType.Undergraduate);
    }

    public void processSelectTA() {
        AppGUIModule gui = app.getGUIModule();
        TableView<TimeSlot> officeHoursTableView = (TableView) gui.getGUINode(CSG_OFFICE_HOURS_TABLE_VIEW);
        officeHoursTableView.refresh();
    }
    
    public void processChangeSiteImage(Pane parent,
            ImageView oldImage, String newImagePath, Object type, 
            AppNodesBuilder builder, String styleClass) {
        //TODO make this into a transaction class
        parent.getChildren().remove(oldImage);
        ImageView newImage = builder.buildImage(type, "file:" + newImagePath, parent, styleClass, ENABLED);
    }
    
    
    
}