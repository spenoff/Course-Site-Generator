package csg.workspace.foolproof;

import djf.modules.AppGUIModule;
import djf.ui.foolproof.FoolproofDesign;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import csg.CourseSiteGeneratorApp;
import static csg.CourseSiteGeneratorOfficeHoursPropertyType.CSG_ADD_TA_BUTTON;
import static csg.CourseSiteGeneratorOfficeHoursPropertyType.CSG_EMAIL_TEXT_FIELD;
import static csg.CourseSiteGeneratorOfficeHoursPropertyType.CSG_END_TIME_COMBO_BOX;
import static csg.CourseSiteGeneratorOfficeHoursPropertyType.CSG_NAME_TEXT_FIELD;
import static csg.CourseSiteGeneratorOfficeHoursPropertyType.CSG_START_TIME_COMBO_BOX;
import csg.data.CourseSiteGeneratorData;
import static csg.workspace.style.CSGStyle.CLASS_CSG_TEXT_FIELD;
import static csg.workspace.style.CSGStyle.CLASS_CSG_TEXT_FIELD_ERROR;
import java.util.ArrayList;
import javafx.scene.control.ComboBox;

public class CourseSiteGeneratorFoolproofDesign implements FoolproofDesign {

    CourseSiteGeneratorApp app;

    public CourseSiteGeneratorFoolproofDesign(CourseSiteGeneratorApp initApp) {
        app = initApp;
    }

    @Override
    public void updateControls() {
        updateAddTAFoolproofDesign();
        updateEditTAFoolproofDesign();
        updateStartEndTime();
    }

    private void updateAddTAFoolproofDesign() {
        AppGUIModule gui = app.getGUIModule();
        
        // FOOLPROOF DESIGN STUFF FOR ADD TA BUTTON
        TextField nameTextField = ((TextField) gui.getGUINode(CSG_NAME_TEXT_FIELD));
        TextField emailTextField = ((TextField) gui.getGUINode(CSG_EMAIL_TEXT_FIELD));
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        CourseSiteGeneratorData data = (CourseSiteGeneratorData) app.getDataComponent();
        Button addTAButton = (Button) gui.getGUINode(CSG_ADD_TA_BUTTON);

        // FIRST, IF NO TYPE IS SELECTED WE'LL JUST DISABLE
        // THE CONTROLS AND BE DONE WITH IT
        boolean isTypeSelected = data.isTATypeSelected();
        if (!isTypeSelected) {
            nameTextField.setDisable(true);
            emailTextField.setDisable(true);
            addTAButton.setDisable(true);
            return;
        } // A TYPE IS SELECTED SO WE'LL CONTINUE
        else {
            nameTextField.setDisable(false);
            emailTextField.setDisable(false);
            addTAButton.setDisable(false);
        }

        // NOW, IS THE USER-ENTERED DATA GOOD?
        boolean isLegalNewTA = data.isLegalNewTA(name, email);

        // ENABLE/DISABLE THE CONTROLS APPROPRIATELY
        addTAButton.setDisable(!isLegalNewTA);
        if (isLegalNewTA) {
            nameTextField.setOnAction(addTAButton.getOnAction());
            emailTextField.setOnAction(addTAButton.getOnAction());
        } else {
            nameTextField.setOnAction(null);
            emailTextField.setOnAction(null);
        }

        // UPDATE THE CONTROL TEXT DISPLAY APPROPRIATELY
        boolean isLegalNewName = data.isLegalNewName(name);
        boolean isLegalNewEmail = data.isLegalNewEmail(email);
        foolproofTextField(nameTextField, isLegalNewName);
        foolproofTextField(emailTextField, isLegalNewEmail);
    }
    
    private void updateEditTAFoolproofDesign() {
        
    }
    
    public void updateStartEndTime(){
        ComboBox startBox = (ComboBox)app.getGUIModule().getGUINode(CSG_START_TIME_COMBO_BOX);
        ComboBox endBox = (ComboBox)app.getGUIModule().getGUINode(CSG_END_TIME_COMBO_BOX);
        CourseSiteGeneratorData data = (CourseSiteGeneratorData) app.getDataComponent();
//        if(startBox.getSelectionModel().getSelectedItem() == null || endBox.getSelectionModel().getSelectedItem() == null){
//            return;
//        }
        ArrayList<String> times = new ArrayList<String>();
        times.add("12:00am");
        for(int j = 1; j <= 11; j++){
            times.add(j + ":00am");
        }
        times.add("12:00pm");
        for(int j = 1; j <= 11; j++){
            times.add(j + ":00pm");
        }
        if(startBox.getSelectionModel().getSelectedItem() != null) {
            String selectedItem = (String) endBox.getSelectionModel().getSelectedItem();
            endBox.getItems().clear();
            for(String time : times){
                String startTime = (String) startBox.getSelectionModel().getSelectedItem();
                if(CourseSiteGeneratorData.timeToHour(time) >= CourseSiteGeneratorData.timeToHour(startTime)){
                    endBox.getItems().add(time);
                }
            }
            if(endBox.getItems().contains(selectedItem)){
                endBox.getSelectionModel().select(selectedItem);
            }
        }
    }
    
    public void foolproofTextField(TextField textField, boolean hasLegalData) {
        if (hasLegalData) {
            textField.getStyleClass().remove(CLASS_CSG_TEXT_FIELD_ERROR);
            if (!textField.getStyleClass().contains(CLASS_CSG_TEXT_FIELD)) {
                textField.getStyleClass().add(CLASS_CSG_TEXT_FIELD);
            }
        } else {
            textField.getStyleClass().remove(CLASS_CSG_TEXT_FIELD);
            if (!textField.getStyleClass().contains(CLASS_CSG_TEXT_FIELD_ERROR)) {
                textField.getStyleClass().add(CLASS_CSG_TEXT_FIELD_ERROR);
            }
        }
    }
}
