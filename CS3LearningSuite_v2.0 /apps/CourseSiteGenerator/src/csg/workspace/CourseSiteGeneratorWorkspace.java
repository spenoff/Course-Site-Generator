package csg.workspace;

import djf.components.AppWorkspaceComponent;
import djf.modules.AppFoolproofModule;
import djf.modules.AppGUIModule;
import static djf.modules.AppGUIModule.ENABLED;
import djf.ui.AppNodesBuilder;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import properties_manager.PropertiesManager;
import csg.CourseSiteGeneratorApp;
import csg.CourseSiteGeneratorOfficeHoursPropertyType;
import static csg.CourseSiteGeneratorOfficeHoursPropertyType.*;
import csg.CourseSiteGeneratorPropertyType;
import static csg.CourseSiteGeneratorPropertyType.*;
import csg.CourseSiteGeneratorSitePropertyType;
import static csg.CourseSiteGeneratorSitePropertyType.*;
import csg.CourseSiteGeneratorSyllabusPropertyType;
import static csg.CourseSiteGeneratorSyllabusPropertyType.*;
import csg.CourseSiteGeneratorSchedulePropertyType;
import static csg.CourseSiteGeneratorSchedulePropertyType.*;
import csg.CourseSiteGeneratorMeetingTimesPropertyType;
import static csg.CourseSiteGeneratorMeetingTimesPropertyType.*;
import csg.data.CourseSiteGeneratorData;
import csg.data.DataProperties;
import csg.data.TeachingAssistantPrototype;
import csg.data.TimeSlot;
import csg.workspace.controllers.CourseSiteGeneratorController;
import csg.workspace.dialogs.TADialog;
import csg.workspace.foolproof.CourseSiteGeneratorFoolproofDesign;
import static csg.workspace.style.CSGStyle.*;
import java.awt.Image;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import javafx.animation.AnimationTimer;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 *
 * @author McKillaGorilla
 */
public class CourseSiteGeneratorWorkspace extends AppWorkspaceComponent {
    // Instance variables related to site pane
    StringProperty exportDirectory = new SimpleStringProperty(".\\export\\");
    String ohTextAreaText = ""; //TODO remove
    StringProperty ohTextAreaTextProperty = new SimpleStringProperty();
    public PrintWriter subjectWriter;
    public PrintWriter numberWriter;
    List<String> subjects = new ArrayList<String>();
    List<String> numbers = new ArrayList<String>();
    
    //Instance variables related to syllabus pane
    StringProperty descriptionTextProperty = new SimpleStringProperty();
    StringProperty topicsTextProperty = new SimpleStringProperty();
    StringProperty prerequisitesTextProperty = new SimpleStringProperty();
    StringProperty outcomesTextProperty = new SimpleStringProperty();
    StringProperty textbooksTextProperty = new SimpleStringProperty();
    StringProperty gradedComponentsTextProperty = new SimpleStringProperty();
    StringProperty gradingNotesTextProperty = new SimpleStringProperty();
    StringProperty academicDishonestyTextProperty = new SimpleStringProperty();
    StringProperty specialAssistanceTextProperty = new SimpleStringProperty();
    
    // Instance variables related to syllabus pane
    
    public CourseSiteGeneratorWorkspace(CourseSiteGeneratorApp app) {
        super(app);
        
       

        // LAYOUT THE APP
        try{
        initLayout();
        } catch(IOException e){
             System.out.println("There seems to be a problem with the file.");
        }

        // INIT THE EVENT HANDLERS
        initControllers();

        // 
        initFoolproofDesign();

        // INIT DIALOGS
        initDialogs();
    }

    private void initDialogs() {
        TADialog taDialog = new TADialog((CourseSiteGeneratorApp) app);
        app.getGUIModule().addDialog(CSG_TA_EDIT_DIALOG, taDialog);
    }

    // THIS HELPER METHOD INITIALIZES ALL THE CONTROLS IN THE WORKSPACE
    private void initLayout() throws IOException {
        // FIRST LOAD THE FONT FAMILIES FOR THE COMBO BOX
        PropertiesManager props = PropertiesManager.getPropertiesManager();

        // THIS WILL BUILD ALL OF OUR JavaFX COMPONENTS FOR US
        AppNodesBuilder csgBuilder = app.getGUIModule().getNodesBuilder();

        HBox mainPane = csgBuilder.buildHBox(CSG_MAIN_PANE, null, CLASS_CSG_PANE, ENABLED);
        //TODO make enums
        String[] tabNames = {props.getProperty("CSG_SITE_TEXT"), 
            props.getProperty("CSG_SYLLABUS_TEXT"), 
            props.getProperty("CSG_MEETING_TIMES_TEXT"), 
            props.getProperty("CSG_OFFICE_HOURS_TEXT"), props.getProperty("CSG_SCHEDULE_TEXT")};
        HashMap<String,Integer> tabIndex = new HashMap<String,Integer>();
        
        int i = 0;
        for(String name : tabNames){
            tabIndex.put(name, i);
            i++;
        }
        //TODO create tab pane style class
        TabPane tabPane = csgBuilder.buildTabPane(CSG_MAIN_TAB_PANE, workspace, CLASS_CSG_PANE, tabNames, ENABLED);
        mainPane.getChildren().add(tabPane);
        
        // <------------------- For Site Pane---------------------------------->
        ScrollPane siteScrollPane = csgBuilder.buildScrollPane(CSG_SITE_SCROLL_PANE, null, CLASS_CSG_SCROLL_PANE, ENABLED);
        siteScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        //VBox siteSuperBox = csgBuilder.buildVBox(CSG_SITE_SUPER_BOX, null, CLASS_CSG_SUPER_BOX, ENABLED);
        
        VBox sitePane = csgBuilder.buildVBox(CSG_SITE_PANE, null, CLASS_CSG_PANE, ENABLED);
        
        siteScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        siteScrollPane.setContent(sitePane);
        sitePane.setSpacing(20);
        VBox bannerPane = csgBuilder.buildVBox(CSG_BANNER_PANE, sitePane, CLASS_CSG_BOX, ENABLED);
        csgBuilder.buildLabel(CSG_BANNER_LABEL, bannerPane, CLASS_CSG_HEADER_LABEL, ENABLED);
        
        HBox bannerBox1 = csgBuilder.buildHBox(CSG_BANNER_BOX1, bannerPane, CLASS_CSG_PANE, ENABLED);
        csgBuilder.buildLabel(CSG_SUBJECT_LABEL, bannerBox1, CLASS_CSG_PANE, ENABLED);
        ComboBox subjCB = csgBuilder.buildComboBox(CSG_SUBJECT_COMBO_BOX, null, null, bannerBox1, CLASS_CSG_PANE, ENABLED);
        subjCB.setEditable(ENABLED);
        subjCB.getItems().add("CSE");
        subjCB.getItems().add("ISE");
        subjects.add("CSE");
        subjects.add("ISE");
        //read from the file
        Scanner reader = new Scanner(new File("Subjects.txt"));
        while(reader.hasNext()){
            String subjectsFromFileText = reader.next();
            String[] subjectsFromFile = subjectsFromFileText.split("\\,");
            for(String subject : subjectsFromFile) {
                if(!subjects.contains(subject) && !subject.trim().equals("")){
                    subjects.add(subject);
                }
            }
        }
        subjCB.getSelectionModel().selectFirst();
        subjectWriter = new PrintWriter(new BufferedWriter(new FileWriter("Subjects.txt")));
        
        for(String subject : subjects) {
            if(!subjCB.getItems().contains(subject)){
                subjCB.getItems().add(subject);
            }
            
            subjectWriter.write("," + subject);
        }
        subjectWriter.flush();
        
        csgBuilder.buildLabel(CSG_NUMBER_LABEL, bannerBox1, CLASS_CSG_PANE, ENABLED);
        ComboBox numCB = csgBuilder.buildComboBox(CSG_NUMBER_COMBO_BOX, null, null, bannerBox1, CLASS_CSG_PANE, ENABLED);
        numCB.setEditable(ENABLED);
        numCB.getItems().add("219");
        numCB.getItems().add("216");
        numbers.add("219");
        numbers.add("216");
        //read from the file
        reader = new Scanner(new File("Numbers.txt"));
        while(reader.hasNext()) {
            String numbersFromFileText = reader.next();
            String[] numbersFromFile = numbersFromFileText.split("\\,");
            for(String number : numbersFromFile) {
                if(!numbers.contains(number) && !number.trim().equals("")) {
                    numbers.add(number);
                }
            }
        }
        numCB.getSelectionModel().selectFirst();
        numberWriter = new PrintWriter(new BufferedWriter(new FileWriter("Numbers.txt")));
        for(String number : numbers) {
            if(!numCB.getItems().contains(number)){
                numCB.getItems().add(number);
            }
            
            numberWriter.write("," + number);
        }
        numberWriter.flush();
        
        HBox bannerBox2 = csgBuilder.buildHBox(CSG_BANNER_BOX2, bannerPane, CLASS_CSG_PANE, ENABLED);
        csgBuilder.buildLabel(CSG_SEMESTER_LABEL, bannerBox2, CLASS_CSG_PANE, ENABLED);
        ComboBox semCB = csgBuilder.buildComboBox(CSG_SEMESTER_COMBO_BOX, null, null, bannerBox2, CLASS_CSG_PANE, ENABLED);
        semCB.getItems().add(props.getProperty("CSG_FALL_TEXT"));
        semCB.getItems().add(props.getProperty("CSG_SPRING_TEXT"));
        semCB.getItems().add(props.getProperty("CSG_WINTER_TEXT"));
        semCB.getItems().add(props.getProperty("CSG_SUMMER_TEXT"));
        semCB.getSelectionModel().selectFirst();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        csgBuilder.buildLabel(CSG_YEAR_LABEL, bannerBox2, CLASS_CSG_PANE, ENABLED);
        ComboBox yearCB = csgBuilder.buildComboBox(CSG_YEAR_COMBO_BOX, null, null, bannerBox2, CLASS_CSG_PANE, ENABLED);
        yearCB.getItems().add(year);
        yearCB.getItems().add(year+1);
        yearCB.getSelectionModel().selectFirst();
        
        HBox bannerBox3 = csgBuilder.buildHBox(CSG_BANNER_BOX3, bannerPane, CLASS_CSG_PANE, ENABLED);
        bannerBox3.setSpacing(50);
        csgBuilder.buildLabel(CSG_SITE_TITLE_LABEL, bannerBox3, CLASS_CSG_LABEL, ENABLED);
        csgBuilder.buildTextField(CSG_SITE_TITLE_TEXT_FIELD, bannerBox3, CLASS_CSG_PANE, ENABLED);
        
        HBox bannerBox4 = csgBuilder.buildHBox(CSG_BANNER_BOX4, bannerPane, CLASS_CSG_PANE, ENABLED);
        bannerBox4.setSpacing(50);
        csgBuilder.buildLabel(CSG_EXPORT_DIR_LABEL, bannerBox4, CLASS_CSG_LABEL, ENABLED);
        csgBuilder.buildLabel(CSG_EXPORT_DIRECTORY, bannerBox4, CLASS_CSG_PANE, ENABLED);
        
        
        
        
        HBox pagesPane = csgBuilder.buildHBox(CSG_PAGES_PANE, sitePane, CLASS_CSG_BOX, ENABLED);
        pagesPane.setSpacing(25);
        csgBuilder.buildLabel(CSG_PAGES_LABEL, pagesPane, CLASS_CSG_HEADER_LABEL, ENABLED);
        csgBuilder.buildCheckBox(CSG_HOME_CHECK_BOX, pagesPane, CLASS_CSG_PANE, ENABLED);
        csgBuilder.buildCheckBox(CSG_SYLLABUS_CHECK_BOX, pagesPane, CLASS_CSG_PANE, ENABLED);
        csgBuilder.buildCheckBox(CSG_SCHEDULE_CHECK_BOX, pagesPane, CLASS_CSG_PANE, ENABLED);
        csgBuilder.buildCheckBox(CSG_HWS_CHECK_BOX, pagesPane, CLASS_CSG_PANE, ENABLED);
        
        VBox stylePane = csgBuilder.buildVBox(CSG_STYLE_PANE, sitePane, CLASS_CSG_BOX, ENABLED);;
        csgBuilder.buildLabel(CSG_STYLE_LABEL, stylePane, CLASS_CSG_HEADER_LABEL, ENABLED);
        HBox faviconBox = csgBuilder.buildHBox(CSG_FAVICON_BOX, stylePane, CLASS_CSG_PANE, ENABLED);
        faviconBox.setSpacing(6);
        csgBuilder.buildTextButton(CSG_FAVICON_BUTTON, faviconBox, CLASS_CSG_PANE, ENABLED);
        ImageView favicon = csgBuilder.buildImage(CSG_FAVICON_IMAGE, "file:./work/favicon.jpeg", faviconBox, CLASS_CSG_FAVICON_IMAGE, ENABLED);
        //ImageView favicon = new ImageView("file:./work/favicon.jpeg");
        //faviconBox.getChildren().add(favicon);
        favicon.setFitHeight(50);
        favicon.setFitWidth(50);
        HBox navbarBox = csgBuilder.buildHBox(CSG_NAVBAR_BOX, stylePane, CLASS_CSG_PANE, ENABLED);
        navbarBox.setSpacing(6);
        csgBuilder.buildTextButton(CSG_NAVBAR_BUTTON, navbarBox, CLASS_CSG_PANE, ENABLED);
        ImageView navbarImage = csgBuilder.buildImage(CSG_NAVBAR_IMAGE, "file:./work/SBUDarkRedShieldLogo.png", navbarBox, CLASS_CSG_PANE, ENABLED);
        HBox leftFooterBox = csgBuilder.buildHBox(CSG_LEFT_FOOTER_BOX, stylePane, CLASS_CSG_PANE, ENABLED);
        leftFooterBox.setSpacing(6);
        
        csgBuilder.buildTextButton(CSG_LEFT_FOOTER_BUTTON, leftFooterBox, CLASS_CSG_PANE, ENABLED);
        ImageView leftFooterImage = csgBuilder.buildImage(CSG_LEFT_FOOTER_IMAGE, "file:./work/SBUWhiteShieldLogo.jpeg", leftFooterBox, CLASS_CSG_PANE, ENABLED);
        
        HBox rightFooterBox = csgBuilder.buildHBox(CSG_RIGHT_FOOTER_BOX, stylePane, CLASS_CSG_PANE, ENABLED);
        rightFooterBox.setSpacing(6);
        csgBuilder.buildTextButton(CSG_RIGHT_FOOTER_BUTTON, rightFooterBox, CLASS_CSG_PANE, ENABLED);
        ImageView rightFooterImage = csgBuilder.buildImage(CSG_RIGHT_FOOTER_IMAGE, "file:./work/SBUCSLogo.png", rightFooterBox, CLASS_CSG_PANE, ENABLED);
        
        HBox cssBox = csgBuilder.buildHBox(CSG_CSS_BOX, stylePane, CLASS_CSG_PANE, ENABLED);
        cssBox.setSpacing(6);
        csgBuilder.buildLabel(CSG_FONTS_AND_COLORS_STYLE_SHEET_LABEL, cssBox, CLASS_CSG_LABEL, ENABLED);
        ComboBox cssComb = csgBuilder.buildComboBox(CSG_FONTS_AND_COLORS_STYLE_SHEET_COMBO_BOX, null, null, cssBox, CLASS_CSG_PANE, ENABLED);
        cssComb.getItems().add("style.css");
        cssComb.getSelectionModel().selectFirst();
        csgBuilder.buildLabel(CSG_NOTE_LABEL, sitePane, CLASS_CSG_LABEL, ENABLED);
        
        
        //Relates to instructor details
        VBox instructorPane = csgBuilder.buildVBox(CSG_INSTRUCTOR_PANE, sitePane, CLASS_CSG_BOX, ENABLED);
        csgBuilder.buildLabel(CSG_INSTRUCTOR_LABEL, instructorPane, CLASS_CSG_HEADER_LABEL, ENABLED);
        HBox instructorBox1 = csgBuilder.buildHBox(CSG_INSTRUCTOR_BOX_1, instructorPane, CLASS_CSG_PANE, ENABLED);
        csgBuilder.buildLabel(CSG_INSTRUCTOR_NAME_LABEL, instructorBox1, CLASS_CSG_LABEL, ENABLED);
        csgBuilder.buildTextField(CSG_INSTRUCTOR_NAME_TEXT_FIELD, instructorBox1, CLASS_CSG_TEXT_FIELD, ENABLED);
        csgBuilder.buildLabel(CSG_INSTRUCTOR_ROOM_LABEL, instructorBox1, CLASS_CSG_LABEL, ENABLED);
        csgBuilder.buildTextField(CSG_INSTRUCTOR_ROOM_TEXT_FIELD, instructorBox1, CLASS_CSG_TEXT_FIELD, ENABLED);
        HBox instructorBox2 = csgBuilder.buildHBox(CSG_INSTRUCTOR_BOX_2, instructorPane, CLASS_CSG_PANE, ENABLED);
        csgBuilder.buildLabel(CSG_INSTRUCTOR_EMAIL_LABEL, instructorBox2, CLASS_CSG_LABEL, ENABLED);
        csgBuilder.buildTextField(CSG_INSTRUCTOR_EMAIL_TEXT_FIELD, instructorBox2, CLASS_CSG_TEXT_FIELD, ENABLED);
        csgBuilder.buildLabel(CSG_HOME_PAGE_LABEL, instructorBox2, CLASS_CSG_LABEL, ENABLED);
        csgBuilder.buildTextField(CSG_HOME_PAGE_TEXT_FIELD, instructorBox2, CLASS_CSG_TEXT_FIELD, ENABLED);
        HBox instructorBox3 = csgBuilder.buildHBox(CSG_INSTRUCTOR_BOX_3, instructorPane, CLASS_CSG_PANE, ENABLED);
        Button instructorOHButton = csgBuilder.buildTextButton(CSG_INSTRUCTOR_PLUS_MINUS_BUTTON, instructorBox3, CLASS_CSG_PANE, ENABLED);
        instructorOHButton.setText("+");
        instructorOHButton.setShape(new Circle(1.5));
        
        
//        csgBuilder.buildLabel(CSG_OFFICE_HOURS_LABEL, instructorBox3, CLASS_CSG_LABEL, ENABLED);
//        TextArea ohTF = csgBuilder.buildTextArea(CSG_OFFICE_HOURS_TEXT_AREA, instructorPane, CLASS_CSG_TEXT_FIELD, ENABLED);
        //ohTF.setDisable(ENABLED);
        //TODO update klingon xml file
        
        // <---------------- For Syllabus Pane ------------------------------>
        VBox syllabusPane = csgBuilder.buildVBox(CSG_SYLLABUS_PANE, null, CLASS_CSG_LABEL, ENABLED);
        
        ScrollPane syllabusScrollPane = csgBuilder.buildScrollPane(CSG_SYLLABUS_SCROLL_PANE, null, CLASS_CSG_SCROLL_PANE, ENABLED);
        syllabusPane.setSpacing(25);
        syllabusScrollPane.setFitToWidth(ENABLED);
        syllabusScrollPane.setContent(syllabusPane);
        VBox descriptionPane = csgBuilder.buildVBox(CSG_DESCRIPTION_PANE, syllabusPane, CLASS_CSG_BOX, ENABLED);
        HBox descriptionLine = csgBuilder.buildHBox(CSG_DESCIPTION_LINE, descriptionPane, CLASS_CSG_PANE, ENABLED);
        descriptionLine.setSpacing(6);
        Button descriptionButton = csgBuilder.buildTextButton(CSG_DESCRIPTION_BUTTON, descriptionLine, CLASS_CSG_PANE, ENABLED);
        descriptionButton.setText("+");
        csgBuilder.buildLabel(CSG_DESCRIPTION_LABEL, descriptionLine, CLASS_CSG_LABEL, ENABLED);
        
        VBox topicsPane = csgBuilder.buildVBox(CSG_TOPICS_PANE, syllabusPane, CLASS_CSG_BOX, ENABLED);
        HBox topicsLine = csgBuilder.buildHBox(CSG_TOPICS_LINE, topicsPane, CLASS_CSG_PANE, ENABLED);
        topicsLine.setSpacing(6);
        Button topicsButton = csgBuilder.buildTextButton(CSG_TOPICS_BUTTON, topicsLine, CLASS_CSG_PANE, ENABLED);
        topicsButton.setText("+");
        csgBuilder.buildLabel(CSG_TOPICS_LABEL, topicsLine, CLASS_CSG_LABEL, ENABLED);
        //TextArea topicsTF = csgBuilder.buildTextArea(CSG_TOPICS_TEXT_FIELD, topicsPane, CLASS_CSG_TEXT_FIELD, ENABLED);
        //topicsTF.setDisable(true);
        
        VBox prerequisitesPane = csgBuilder.buildVBox(CSG_PREREQUISITES_PANE, syllabusPane, CLASS_CSG_BOX, ENABLED);
        HBox prerequisitesLine = csgBuilder.buildHBox(CSG_PREREQUISITES_LINE, prerequisitesPane, CLASS_CSG_PANE, ENABLED);
        prerequisitesLine.setSpacing(6);
        Button prerequisitesButton = csgBuilder.buildTextButton(CSG_PREREQUISITES_BUTTON, prerequisitesLine, CLASS_CSG_PANE, ENABLED);
        prerequisitesButton.setText("+");
        csgBuilder.buildLabel(CSG_PREREQUISITES_LABEL, prerequisitesLine, CLASS_CSG_LABEL, ENABLED);
        //TextArea prereqTF = csgBuilder.buildTextArea(CSG_PREREQUISITES_TEXT_FIELD, prerequisitesPane, CLASS_CSG_TEXT_FIELD, ENABLED);
        //prereqTF.setDisable(true);
        
        VBox outcomesPane = csgBuilder.buildVBox(CSG_OUTCOMES_PANE, syllabusPane, CLASS_CSG_BOX, ENABLED);
        HBox outcomesLine = csgBuilder.buildHBox(CSG_OUTCOMES_LINE, outcomesPane, CLASS_CSG_PANE, ENABLED);
        outcomesLine.setSpacing(6);
        Button outcomesButton = csgBuilder.buildTextButton(CSG_OUTCOMES_BUTTON, outcomesLine, CLASS_CSG_PANE, ENABLED);
        outcomesButton.setText("+");
        csgBuilder.buildLabel(CSG_OUTCOMES_LABEL, outcomesLine, CLASS_CSG_LABEL, ENABLED);
        //TextArea outcomesTF = csgBuilder.buildTextArea(CSG_OUTCOMES_TEXT_FIELD, outcomesPane, CLASS_CSG_TEXT_FIELD, ENABLED);
        //outcomesTF.setDisable(true);
        
        VBox textbooksPane = csgBuilder.buildVBox(CSG_TEXTBOOKS_PANE, syllabusPane, CLASS_CSG_BOX, ENABLED);
        HBox textbooksLine = csgBuilder.buildHBox(CSG_TEXTBOOKS_LINE, textbooksPane, CLASS_CSG_PANE, ENABLED);
        textbooksLine.setSpacing(6);
        Button textbooksButton = csgBuilder.buildTextButton(CSG_TEXTBOOKS_BUTTON, textbooksLine, CLASS_CSG_PANE, ENABLED);
        textbooksButton.setText("+");
        csgBuilder.buildLabel(CSG_TEXTBOOKS_LABEL, textbooksLine, CLASS_CSG_LABEL, ENABLED);
        //TextArea textbooksTF = csgBuilder.buildTextArea(CSG_TEXTBOOKS_TEXT_FIELD, textbooksPane, CLASS_CSG_TEXT_FIELD, ENABLED);
        //textbooksTF.setDisable(true);
        
        VBox gradedComponentsPane = csgBuilder.buildVBox(CSG_GRADED_COMPONENTS_PANE, syllabusPane, CLASS_CSG_BOX, ENABLED);
        HBox gradedComponentsLine = csgBuilder.buildHBox(CSG_GRADED_COMPONENTS_LINE, gradedComponentsPane, CLASS_CSG_PANE, ENABLED);
        gradedComponentsLine.setSpacing(6);
        Button gradedComponentsButton = csgBuilder.buildTextButton(CSG_GRADED_COMPONENTS_BUTTON, gradedComponentsLine, CLASS_CSG_PANE, ENABLED);
        gradedComponentsButton.setText("+");
        csgBuilder.buildLabel(CSG_GRADED_COMPONENTS_LABEL, gradedComponentsLine, CLASS_CSG_LABEL, ENABLED);
        //TextArea gradedComponentsTF = csgBuilder.buildTextArea(CSG_GRADED_COMPONENTS_TEXT_FIELD, gradedComponentsPane, CLASS_CSG_TEXT_FIELD, ENABLED);
        //gradedComponentsTF.setDisable(true);
        
        VBox gradingNotePane = csgBuilder.buildVBox(CSG_GRADING_NOTE_PANE, syllabusPane, CLASS_CSG_BOX, ENABLED);
        HBox gradingNotesLine = csgBuilder.buildHBox(CSG_GRADING_NOTE_LINE, gradingNotePane, CLASS_CSG_PANE, ENABLED);
        gradingNotesLine.setSpacing(6);
        Button gradingNotesButton = csgBuilder.buildTextButton(CSG_GRADING_NOTE_BUTTON, gradingNotesLine, CLASS_CSG_PANE, ENABLED);
        gradingNotesButton.setText("+");
        csgBuilder.buildLabel(CSG_GRADING_NOTE_LABEL, gradingNotesLine, CLASS_CSG_LABEL, ENABLED);
        //TextArea gradedNoteTF = csgBuilder.buildTextArea(CSG_GRADING_NOTE_TEXT_FIELD, gradingNotePane, CLASS_CSG_TEXT_FIELD, ENABLED);
        //gradedNoteTF.setDisable(true);
        
        VBox academicDishonestyPane = csgBuilder.buildVBox(CSG_ACADEMIC_DISHONESTY_PANE, syllabusPane, CLASS_CSG_BOX, ENABLED);
        HBox academicDishonestyLine = csgBuilder.buildHBox(CSG_ACADEMIC_DISHONESTY_LINE, academicDishonestyPane, CLASS_CSG_PANE, ENABLED);
        academicDishonestyLine.setSpacing(6);
        Button academicDishonestyButton = csgBuilder.buildTextButton(CSG_ACADEMIC_DISHONESTY_BUTTON, academicDishonestyLine, CLASS_CSG_PANE, ENABLED);
        academicDishonestyButton.setText("+");
        csgBuilder.buildLabel(CSG_ACADEMIC_DISHONESTY_LABEL, academicDishonestyLine, CLASS_CSG_LABEL, ENABLED);
        //TextArea adTF = csgBuilder.buildTextArea(CSG_ACADEMIC_DISHONESTY_TEXT_FIELD, academicDishonestyPane, CLASS_CSG_TEXT_FIELD, ENABLED);
        //adTF.setDisable(true);
        
        VBox specialAssistancePane = csgBuilder.buildVBox(CSG_SPECIAL_ASSISTANCE_PANE, syllabusPane, CLASS_CSG_BOX, ENABLED);
        HBox specialAssistanceLine = csgBuilder.buildHBox(CSG_SPECIAL_ASSISTANCE_LINE, specialAssistancePane, CLASS_CSG_PANE, ENABLED);
        specialAssistanceLine.setSpacing(6);
        Button specialAssistanceButton = csgBuilder.buildTextButton(CSG_SPECIAL_ASSISTANCE_BUTTON, specialAssistanceLine, CLASS_CSG_PANE, ENABLED);
        specialAssistanceButton.setText("+");
        csgBuilder.buildLabel(CSG_SPECIAL_ASSISTANCE_LABEL, specialAssistanceLine, CLASS_CSG_LABEL, ENABLED);
        //TextArea specialAssistanceTF = csgBuilder.buildTextArea(CSG_SPECIAL_ASSISTANCE_TEXT_FIELD, specialAssistancePane, CLASS_CSG_TEXT_FIELD, ENABLED);
        //specialAssistanceTF.setDisable(true);
        
        //------------------ For Meeting Times Pane ------------------------->
        ScrollPane meetingTimesScrollPane = csgBuilder.buildScrollPane(CSG_MEETING_TIMES_SCROLL_PANE, null, CLASS_CSG_SCROLL_PANE, ENABLED);
        meetingTimesScrollPane.setFitToWidth(ENABLED);
        VBox meetingTimesPane = csgBuilder.buildVBox(CSG_MEETING_TIMES_PANE, null, CLASS_CSG_PANE, ENABLED);
        meetingTimesScrollPane.setContent(meetingTimesPane);
        meetingTimesPane.setSpacing(6);
        VBox lecturesPane = csgBuilder.buildVBox(CSG_LECTURES_PANE, meetingTimesPane, CLASS_CSG_BOX, ENABLED);
        HBox lecturesBox = csgBuilder.buildHBox(CSG_LECTURES_BOX, lecturesPane, CLASS_CSG_PANE, ENABLED);
        lecturesBox.setSpacing(6);
        csgBuilder.buildTextButton(CSG_LECTURES_PLUS_BUTTON, lecturesBox, CLASS_CSG_BUTTON, ENABLED);
        csgBuilder.buildTextButton(CSG_LECTURES_MINUS_BUTTON, lecturesBox, CLASS_CSG_BUTTON, ENABLED);
        csgBuilder.buildLabel(CSG_LECTURES_LABEL, lecturesBox, CLASS_CSG_BUTTON, ENABLED);
        TableView lecturesTable = csgBuilder.buildTableView(CSG_LECTURES_TABLE_VIEW, lecturesPane, CLASS_CSG_TABLE_VIEW, ENABLED);
        TableColumn sectionColumn = csgBuilder.buildTableColumn(CSG_SECTION_COLUMN, lecturesTable, CLASS_CSG_COLUMN);
        TableColumn daysColumn = csgBuilder.buildTableColumn(CSG_DAYS_COLUMN, lecturesTable, CLASS_CSG_COLUMN);
        TableColumn timeColumn = csgBuilder.buildTableColumn(CSG_TIME_COLUMN, lecturesTable, CLASS_CSG_CENTERED_COLUMN);
        TableColumn roomColumn = csgBuilder.buildTableColumn(CSG_ROOM_COLUMN, lecturesTable, CLASS_CSG_COLUMN);
        sectionColumn.setCellValueFactory(new PropertyValueFactory<String, String>("section"));
        daysColumn.setCellValueFactory(new PropertyValueFactory<String, String>("days"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<String, String>("time"));
        roomColumn.setCellValueFactory(new PropertyValueFactory<String, String>("room"));
        
        
        VBox recitationsPane = csgBuilder.buildVBox(CSG_RECITATIONS_PANE, meetingTimesPane, CLASS_CSG_BOX, ENABLED);
        HBox recitationsBox = csgBuilder.buildHBox(CSG_RECITATIONS_PANE, recitationsPane, CLASS_CSG_BOX, ENABLED);
        recitationsBox.setSpacing(6);
        csgBuilder.buildTextButton(CSG_RECITATIONS_PLUS_BUTTON, recitationsBox, CLASS_CSG_BUTTON, ENABLED);
        csgBuilder.buildTextButton(CSG_RECITATIONS_MINUS_BUTTON, recitationsBox, CLASS_CSG_BUTTON, ENABLED);
        csgBuilder.buildLabel(CSG_RECITATIONS_LABEL, recitationsBox, CLASS_CSG_BUTTON, ENABLED);
        TableView recitationsTable = csgBuilder.buildTableView(CSG_RECITATIONS_TABLE_VIEW, recitationsPane, CLASS_CSG_TABLE_VIEW, ENABLED);
        recitationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn recitationsSectionColumn = csgBuilder.buildTableColumn(CSG_RECITATIONS_SECTION_COLUMN, recitationsTable, CLASS_CSG_COLUMN);
        TableColumn recitationsDaysAndTimesColumn = csgBuilder.buildTableColumn(CSG_RECITATIONS_DAYS_AND_TIMES_COLUMN, recitationsTable, CLASS_CSG_COLUMN);
        TableColumn recitationRoomColumn = csgBuilder.buildTableColumn(CSG_RECITATIONS_ROOM_COLUMN, recitationsTable, CLASS_CSG_CENTERED_COLUMN);
        TableColumn recitationTA1Column = csgBuilder.buildTableColumn(CSG_RECITATIONS_TA1_COLUMN, recitationsTable, CLASS_CSG_COLUMN);
        TableColumn recitationTA2Column = csgBuilder.buildTableColumn(CSG_RECITATIONS_TA2_COLUMN, recitationsTable, CLASS_CSG_COLUMN);
        recitationsSectionColumn.prefWidthProperty().bind(recitationsTable.widthProperty().multiply(2.0 / 6.0));
        recitationsDaysAndTimesColumn.prefWidthProperty().bind(recitationsTable.widthProperty().multiply(2.0 / 6.0));
        recitationRoomColumn.prefWidthProperty().bind(recitationsTable.widthProperty().multiply(1.0 / 6.0));
        recitationTA1Column.prefWidthProperty().bind(recitationsTable.widthProperty().multiply(1.0 / 6.0));
        recitationTA2Column.prefWidthProperty().bind(recitationsTable.widthProperty().multiply(1.0 / 6.0));
        
        VBox labsPane = csgBuilder.buildVBox(CSG_LABS_PANE, meetingTimesPane, CLASS_CSG_BOX, ENABLED);
        HBox labsBox = csgBuilder.buildHBox(CSG_LABS_PANE, labsPane, CLASS_CSG_BOX, ENABLED);
        labsBox.setSpacing(6);
        csgBuilder.buildTextButton(CSG_LABS_PLUS_BUTTON, labsBox, CLASS_CSG_BUTTON, ENABLED);
        csgBuilder.buildTextButton(CSG_LABS_MINUS_BUTTON, labsBox, CLASS_CSG_BUTTON, ENABLED);
        csgBuilder.buildLabel(CSG_LABS_LABEL, labsBox, CLASS_CSG_BUTTON, ENABLED);
        TableView labsTable = csgBuilder.buildTableView(CSG_LABS_TABLE_VIEW, labsPane, CLASS_CSG_TABLE_VIEW, ENABLED);
        labsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn labsSectionColumn = csgBuilder.buildTableColumn(CSG_LABS_SECTION_COLUMN, labsTable, CLASS_CSG_COLUMN);
        TableColumn labsDaysAndTimesColumn = csgBuilder.buildTableColumn(CSG_LABS_DAYS_AND_TIMES_COLUMN, labsTable, CLASS_CSG_COLUMN);
        TableColumn labsRoomColumn = csgBuilder.buildTableColumn(CSG_LABS_ROOM_COLUMN, labsTable, CLASS_CSG_CENTERED_COLUMN);
        TableColumn labsTA1Column = csgBuilder.buildTableColumn(CSG_LABS_TA1_COLUMN, labsTable, CLASS_CSG_COLUMN);
        TableColumn labsTA2Column = csgBuilder.buildTableColumn(CSG_LABS_TA2_COLUMN, labsTable, CLASS_CSG_COLUMN);
        labsSectionColumn.prefWidthProperty().bind(recitationsTable.widthProperty().multiply(2.0 / 6.0));
        labsDaysAndTimesColumn.prefWidthProperty().bind(recitationsTable.widthProperty().multiply(2.0 / 6.0));
        labsRoomColumn.prefWidthProperty().bind(recitationsTable.widthProperty().multiply(1.0 / 6.0));
        labsTA1Column.prefWidthProperty().bind(recitationsTable.widthProperty().multiply(1.0 / 6.0));
        labsTA2Column.prefWidthProperty().bind(recitationsTable.widthProperty().multiply(1.0 / 6.0));
        
        HBox.setHgrow(recitationsTable, Priority.ALWAYS);
        VBox.setVgrow(recitationsTable, Priority.ALWAYS);
        
        // <---------------- For Office Hours Pane -------------------------->
        
        // INIT THE HEADER ON THE TOP
        
        
        VBox leftPane = csgBuilder.buildVBox(CSG_LEFT_PANE, null, CLASS_CSG_OH_PANE, ENABLED);
        HBox tasHeaderBox = csgBuilder.buildHBox(CSG_TAS_HEADER_PANE, leftPane, CLASS_CSG_BOX, ENABLED);
        csgBuilder.buildTextButton(CSG_TA_MINUS_BUTTON, tasHeaderBox, CLASS_CSG_BUTTON, ENABLED);
        csgBuilder.buildLabel(CourseSiteGeneratorOfficeHoursPropertyType.CSG_TAS_HEADER_LABEL, tasHeaderBox, CLASS_CSG_HEADER_LABEL, ENABLED);
        
        HBox typeHeaderBox = csgBuilder.buildHBox(CSG_GRAD_UNDERGRAD_TAS_PANE, tasHeaderBox, CLASS_CSG_RADIO_BOX, ENABLED);
        
        ToggleGroup tg = new ToggleGroup();
        csgBuilder.buildRadioButton(CSG_ALL_RADIO_BUTTON, typeHeaderBox, CLASS_CSG_RADIO_BUTTON, ENABLED, tg, true);
        
        csgBuilder.buildRadioButton(CSG_GRADUATE_RADIO_BUTTON, typeHeaderBox, CLASS_CSG_RADIO_BUTTON, ENABLED, tg, false);
        csgBuilder.buildRadioButton(CSG_UNDERGRADUATE_RADIO_BUTTON, typeHeaderBox, CLASS_CSG_RADIO_BUTTON, ENABLED, tg, false);

        // MAKE THE TABLE AND SETUP THE DATA MODEL
        TableView<TeachingAssistantPrototype> taTable = csgBuilder.buildTableView(CSG_TAS_TABLE_VIEW, leftPane, CLASS_CSG_TABLE_VIEW, ENABLED);
        taTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        TableColumn nameColumn = csgBuilder.buildTableColumn(CSG_NAME_TABLE_COLUMN, taTable, CLASS_CSG_COLUMN);
        TableColumn emailColumn = csgBuilder.buildTableColumn(CSG_EMAIL_TABLE_COLUMN, taTable, CLASS_CSG_COLUMN);
        TableColumn slotsColumn = csgBuilder.buildTableColumn(CSG_SLOTS_TABLE_COLUMN, taTable, CLASS_CSG_CENTERED_COLUMN);
        TableColumn typeColumn = csgBuilder.buildTableColumn(CSG_TYPE_TABLE_COLUMN, taTable, CLASS_CSG_COLUMN);
        nameColumn.setCellValueFactory(new PropertyValueFactory<String, String>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<String, String>("email"));
        slotsColumn.setCellValueFactory(new PropertyValueFactory<String, String>("slots"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<String, String>("type"));
        nameColumn.prefWidthProperty().bind(taTable.widthProperty().multiply(1.0 / 5.0));
        emailColumn.prefWidthProperty().bind(taTable.widthProperty().multiply(2.0 / 5.0));
        slotsColumn.prefWidthProperty().bind(taTable.widthProperty().multiply(1.0 / 5.0));
        typeColumn.prefWidthProperty().bind(taTable.widthProperty().multiply(1.0 / 5.0));

        // ADD BOX FOR ADDING A TA
        HBox taBox = csgBuilder.buildHBox(CSG_ADD_TA_PANE, leftPane, CLASS_CSG_PANE, ENABLED);
        csgBuilder.buildTextField(CSG_NAME_TEXT_FIELD, taBox, CLASS_CSG_TEXT_FIELD, ENABLED);
        csgBuilder.buildTextField(CSG_EMAIL_TEXT_FIELD, taBox, CLASS_CSG_TEXT_FIELD, ENABLED);
        csgBuilder.buildTextButton(CSG_ADD_TA_BUTTON, taBox, CLASS_CSG_BUTTON, !ENABLED);
        
        // MAKE SURE IT'S THE TABLE THAT ALWAYS GROWS IN THE LEFT PANE
        VBox.setVgrow(taTable, Priority.ALWAYS);

        // INIT THE HEADER ON THE BOTTOM
        VBox rightPane = csgBuilder.buildVBox(CSG_RIGHT_PANE, null, CLASS_CSG_OH_PANE, ENABLED);
        
        HBox officeHoursHeaderBox = csgBuilder.buildHBox(CSG_OFFICE_HOURS_HEADER_PANE, rightPane, CLASS_CSG_BOX, ENABLED);
        officeHoursHeaderBox.setSpacing(150);
        csgBuilder.buildLabel(CSG_OFFICE_HOURS_HEADER_LABEL, officeHoursHeaderBox, CLASS_CSG_HEADER_LABEL, ENABLED);
        HBox ohTimeBox = csgBuilder.buildHBox(CSG_OH_TIME_BOX, officeHoursHeaderBox, CLASS_CSG_PANE, ENABLED);
        ohTimeBox.setSpacing(6);
        csgBuilder.buildLabel(CSG_START_TIME_LABEL, ohTimeBox, CLASS_CSG_HEADER_LABEL, ENABLED);
        ComboBox startBox =  csgBuilder.buildComboBox(CSG_START_TIME_COMBO_BOX, null, null, ohTimeBox, CLASS_CSG_PANE, ENABLED);
        csgBuilder.buildLabel(CSG_END_TIME_LABEL, ohTimeBox, CLASS_CSG_HEADER_LABEL, ENABLED);
        ComboBox endBox = csgBuilder.buildComboBox(CSG_END_TIME_COMBO_BOX, null, null, ohTimeBox, CLASS_CSG_PANE, ENABLED);
        startBox.getItems().add("12:00am");
        endBox.getItems().add("12:00am");
        for(int j = 1; j <= 11; j++) {
            startBox.getItems().add(j + ":00am");
            endBox.getItems().add(j +":00am");
        }
        startBox.getItems().add("12:00pm");
        endBox.getItems().add("12:00pm");
                for(int j = 1; j <= 11; j++) {
            startBox.getItems().add(j + ":00pm");
            endBox.getItems().add(j + ":00pm");
        }
        //The code below caused a glitch with the toggle office hours.
        //The edit time range feature is therefore under maintenance.
        startBox.getSelectionModel().select("9:00am");
        endBox.getSelectionModel().select("9:00pm");
//        
        
        // SETUP THE OFFICE HOURS TABLE
        TableView<TimeSlot> officeHoursTable = csgBuilder.buildTableView(CSG_OFFICE_HOURS_TABLE_VIEW, rightPane, CLASS_CSG_OFFICE_HOURS_TABLE_VIEW, ENABLED);
        officeHoursTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        officeHoursTable.getSelectionModel().setCellSelectionEnabled(ENABLED);
        setupCSGColumn(CSG_START_TIME_TABLE_COLUMN, officeHoursTable, CLASS_CSG_TIME_COLUMN, "startTime");
        setupCSGColumn(CSG_END_TIME_TABLE_COLUMN, officeHoursTable, CLASS_CSG_TIME_COLUMN, "endTime");
        setupCSGColumn(CSG_MONDAY_TABLE_COLUMN, officeHoursTable, CLASS_CSG_DAY_OF_WEEK_COLUMN, "monday");
        setupCSGColumn(CSG_TUESDAY_TABLE_COLUMN, officeHoursTable, CLASS_CSG_DAY_OF_WEEK_COLUMN, "tuesday");
        setupCSGColumn(CSG_WEDNESDAY_TABLE_COLUMN, officeHoursTable, CLASS_CSG_DAY_OF_WEEK_COLUMN, "wednesday");
        setupCSGColumn(CSG_THURSDAY_TABLE_COLUMN, officeHoursTable, CLASS_CSG_DAY_OF_WEEK_COLUMN, "thursday");
        setupCSGColumn(CSG_FRIDAY_TABLE_COLUMN, officeHoursTable, CLASS_CSG_DAY_OF_WEEK_COLUMN, "friday");
        officeHoursTable.getSelectionModel().setCellSelectionEnabled(ENABLED);
        officeHoursTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // MAKE SURE IT'S THE TABLE THAT ALWAYS GROWS IN THE LEFT PANE
        VBox.setVgrow(officeHoursTable, Priority.ALWAYS);

        // BOTH PANES WILL NOW GO IN A VBOX
        
        VBox ohBox = new VBox();
        ohBox.getChildren().add(leftPane);
        ohBox.getChildren().add(rightPane);
        
        // <------------------------------- For Schedule Pane ------------------------------------------->
        ScrollPane scheduleScrollPane = csgBuilder.buildScrollPane(CSG_SCHEDULE_SCROLL_PANE, null, CLASS_CSG_SCROLL_PANE, ENABLED);
        VBox schedulePane = csgBuilder.buildVBox(CSG_SCHEDULE_PANE, null, CLASS_CSG_PANE, ENABLED);
        schedulePane.setSpacing(6);
        scheduleScrollPane.setFitToWidth(ENABLED);
        scheduleScrollPane.setContent(schedulePane);
        VBox calandarBoundariesPane = csgBuilder.buildVBox(CSG_CALANDAR_BOUNDARIES_PANE, schedulePane, CLASS_CSG_BOX, ENABLED);
        csgBuilder.buildLabel(CSG_CALANDAR_BOUNDARIES_LABEL, calandarBoundariesPane, CLASS_CSG_HEADER_LABEL, ENABLED);
        HBox startEndBox = csgBuilder.buildHBox(CSG_START_END_BOX, calandarBoundariesPane, CLASS_CSG_PANE, ENABLED);
        csgBuilder.buildLabel(CSG_STARTING_MONDAY_LABEL, startEndBox, CLASS_CSG_PANE, ENABLED);
        csgBuilder.buildDatePicker(CSG_STARTING_MONDAY_DATE_PICKER, startEndBox, CLASS_CSG_PANE, ENABLED);
        csgBuilder.buildLabel(CSG_ENDING_FRIDAY_LABEL, startEndBox, CLASS_CSG_PANE, ENABLED);
        csgBuilder.buildDatePicker(CSG_ENDING_FRIDAY_DATE_PICKER, startEndBox, CLASS_CSG_PANE, ENABLED);
        VBox scheduleItemsPane = csgBuilder.buildVBox(CSG_SCHEDULE_ITEMS_PANE, schedulePane, CLASS_CSG_BOX, ENABLED);
        HBox scheduleItemsBox = csgBuilder.buildHBox(CSG_SCHEDULE_ITEMS_BOX, scheduleItemsPane, CLASS_CSG_PANE, ENABLED);
        scheduleItemsBox.setSpacing(6);
        csgBuilder.buildTextButton(CSG_SCHEDULE_ITEMS_BUTTON, scheduleItemsBox, CLASS_CSG_BUTTON, ENABLED);
        csgBuilder.buildLabel(CSG_SCHEDULE_ITEMS_LABEL, scheduleItemsBox, CLASS_CSG_HEADER_LABEL, ENABLED);
        TableView scheduleItemsTable = csgBuilder.buildTableView(CSG_SCHEDULE_ITEMS_TABLE_VIEW, scheduleItemsPane, CLASS_CSG_TABLE_VIEW, ENABLED);
        TableColumn scheduleTypeColumn = csgBuilder.buildTableColumn(CSG_SCHEDULE_TYPE_COLUMN, scheduleItemsTable, CLASS_CSG_COLUMN);
        TableColumn scheduleDateColumn = csgBuilder.buildTableColumn(CSG_SCHEDULE_DATE_COLUMN, scheduleItemsTable, CLASS_CSG_COLUMN);
        TableColumn scheduleTitleColumn = csgBuilder.buildTableColumn(CSG_SCHEDULE_TITLE_COLUMN, scheduleItemsTable, CLASS_CSG_COLUMN);
        TableColumn scheduleTopicColumn = csgBuilder.buildTableColumn(CSG_SCHEDULE_TOPIC_COLUMN, scheduleItemsTable, CLASS_CSG_COLUMN);
        scheduleTypeColumn.prefWidthProperty().bind(taTable.widthProperty().multiply(1.0 / 5.0));
        scheduleDateColumn.prefWidthProperty().bind(taTable.widthProperty().multiply(1.0 / 5.0));
        scheduleTitleColumn.prefWidthProperty().bind(taTable.widthProperty().multiply(1.0 / 5.0));
        scheduleTopicColumn.prefWidthProperty().bind(taTable.widthProperty().multiply(2.0 / 5.0));
        
        VBox addEditPane = csgBuilder.buildVBox(CSG_ADD_EDIT_PANE, schedulePane, CLASS_CSG_BOX, ENABLED);
        csgBuilder.buildLabel(CSG_ADD_EDIT_LABEL, addEditPane, CLASS_CSG_HEADER_LABEL, ENABLED);
        HBox scheduleTypeBox = csgBuilder.buildHBox(CSG_SCHEDULE_TYPE_BOX, addEditPane, CLASS_CSG_PANE, ENABLED);
        scheduleTypeBox.setSpacing(30);
        csgBuilder.buildLabel(CSG_SCHEDULE_TYPE_LABEL, scheduleTypeBox, CLASS_CSG_LABEL, ENABLED);
        ComboBox typeCB = csgBuilder.buildComboBox(CSG_SCHEDULE_TYPE_COMBO_BOX, null, null, scheduleTypeBox, CLASS_CSG_PANE, ENABLED);
        typeCB.getItems().add(props.getProperty("CSG_HOLIDAY_TEXT"));
        typeCB.getItems().add(props.getProperty("CSG_LECTURE_TEXT"));
        typeCB.getItems().add(props.getProperty("CSG_RECITATION_TEXT"));
        typeCB.getItems().add(props.getProperty("CSG_LAB_TEXT"));
        typeCB.getItems().add(props.getProperty("CSG_EXAM_TEXT"));
        typeCB.getItems().add(props.getProperty("CSG_HW_TEXT"));
        
        HBox scheduleDateBox = csgBuilder.buildHBox(CSG_SCHEDULE_DATE_BOX, addEditPane, CLASS_CSG_PANE, ENABLED);
        scheduleDateBox.setSpacing(30);
        csgBuilder.buildLabel(CSG_SCHEDULE_DATE_LABEL, scheduleDateBox, CLASS_CSG_LABEL, ENABLED);
        csgBuilder.buildDatePicker(CSG_SCHEDULE_DATE_DATE_PICKER, scheduleDateBox, CLASS_CSG_PANE, ENABLED);
        
        HBox scheduleTitleBox = csgBuilder.buildHBox(CSG_SCHEDULE_TITLE_BOX, addEditPane, CLASS_CSG_PANE, ENABLED);
        scheduleTitleBox.setSpacing(30);
        csgBuilder.buildLabel(CSG_SCHEDULE_TITLE_LABEL, scheduleTitleBox, CLASS_CSG_LABEL, ENABLED);
        csgBuilder.buildTextField(CSG_SCHEDULE_TITLE_TEXT_FIELD, scheduleTitleBox, CLASS_CSG_TEXT_FIELD, ENABLED);
        
        HBox scheduleTopicBox = csgBuilder.buildHBox(CSG_SCHEDULE_TOPIC_BOX, addEditPane, CLASS_CSG_PANE, ENABLED);
        scheduleTopicBox.setSpacing(30);
        csgBuilder.buildLabel(CSG_SCHEDULE_TOPIC_LABEL, scheduleTopicBox, CLASS_CSG_LABEL, ENABLED);
        csgBuilder.buildTextField(CSG_SCHEDULE_TOPIC_TEXT_FIELD, scheduleTopicBox, CLASS_CSG_TEXT_FIELD, ENABLED);
        
        HBox scheduleLinkBox = csgBuilder.buildHBox(CSG_SCHEDULE_LINK_BOX, addEditPane, CLASS_CSG_PANE, ENABLED);
        scheduleLinkBox.setSpacing(30);
        csgBuilder.buildLabel(CSG_SCHEDULE_LINK_LABEL, scheduleLinkBox, CLASS_CSG_LABEL, ENABLED);
        csgBuilder.buildTextField(CSG_SCHEDULE_LINK_TEXT_FIELD, scheduleLinkBox, CLASS_CSG_TEXT_FIELD, ENABLED);
        
        HBox scheduleButtonBox = csgBuilder.buildHBox(CSG_SCHEDULE_BUTTON_BOX, addEditPane, CLASS_CSG_PANE, ENABLED);
        scheduleButtonBox.setSpacing(30);
        csgBuilder.buildTextButton(CSG_SCHEDULE_ADD_UPDATE_BUTTON, scheduleButtonBox, CLASS_CSG_BUTTON, ENABLED);
        csgBuilder.buildTextButton(CSG_SCHEDULE_CLEAR_BUTTON, scheduleButtonBox, CLASS_CSG_BUTTON, ENABLED);

// NOW WE ADD THE MAIN PANES TO THE TAB PANE
        
        Tab siteTab = tabPane.getTabs().get(0);
        siteTab.setContent(siteScrollPane);
        Tab syllabusTab = tabPane.getTabs().get(1);
        syllabusTab.setContent(syllabusScrollPane);
        Tab meetingTimesTab = tabPane.getTabs().get(2);
        meetingTimesTab.setContent(meetingTimesScrollPane);
        Tab ohTab = tabPane.getTabs().get(3);
        ohTab.setContent(ohBox);
        Tab scheduleTab = tabPane.getTabs().get(4);
        scheduleTab.setContent(scheduleScrollPane);
        workspace = new BorderPane();

        // AND PUT EVERYTHING IN THE WORKSPACE
        ((BorderPane) workspace).setCenter(mainPane);
    }

    private void setupCSGColumn(Object columnId, TableView tableView, String styleClass, String columnDataProperty) {
        AppNodesBuilder builder = app.getGUIModule().getNodesBuilder();
        TableColumn<TeachingAssistantPrototype, String> column = builder.buildTableColumn(columnId, tableView, styleClass);
        column.setCellValueFactory(new PropertyValueFactory<TeachingAssistantPrototype, String>(columnDataProperty));
        column.prefWidthProperty().bind(tableView.widthProperty().multiply(1.0 / 7.0));
        column.setCellFactory(col -> {
            return new TableCell<TeachingAssistantPrototype, String>() {
                @Override
                protected void updateItem(String text, boolean empty) {
                    super.updateItem(text, empty);
                    //super.getTableRow().disableProperty().setValue(empty);
                    if (text == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        // CHECK TO SEE IF text CONTAINS THE NAME OF
                        // THE CURRENTLY SELECTED TA
                        setText(text);
                        TableView<TeachingAssistantPrototype> tasTableView = (TableView) app.getGUIModule().getGUINode(CSG_TAS_TABLE_VIEW);
                        TeachingAssistantPrototype selectedTA = tasTableView.getSelectionModel().getSelectedItem();
                        if (selectedTA == null) {
                            setStyle("");
                        } else if (text.contains(selectedTA.getName())) {
                            setStyle("-fx-background-color: yellow");
                        } else {
                            setStyle("");
                        }
                    }
                }
            };
        });
    }

    public void initControllers() {
        CourseSiteGeneratorController controller = new CourseSiteGeneratorController((CourseSiteGeneratorApp) app);
        AppGUIModule gui = app.getGUIModule();
        CourseSiteGeneratorData data = (CourseSiteGeneratorData)app.getDataComponent();
        
        // <------------------------ FOR SITE PANE ---------------------------->
        
        //This is done at the beginning to ensure that the text is always correct
        controller.processUpdateExportDirectory();
        
        VBox sitePane = (VBox)gui.getGUINode(CSG_SITE_PANE);
        sitePane.setOnMouseClicked(e -> {
            controller.processUpdateExportDirectory();
        });
        
      //  ((ComboBox)gui.getGUINode(CSG_SUBJECT_COMBO_BOX)).getSelectionModel().select(DataProperties.subjectText.getValue());
        ((TextField)gui.getGUINode(CSG_SITE_TITLE_TEXT_FIELD)).setText(DataProperties.titleText.getValue());
         DataProperties.titleText.bindBidirectional(((TextField)gui.getGUINode(CSG_SITE_TITLE_TEXT_FIELD)).textProperty());
      
        //File faviconFile = new File(DataProperties.faviconPathText.getValue());
        if((DataProperties.faviconPathText.getValue() != null && !DataProperties.faviconPathText.getValue().trim().equals(""))) {
            ImageView currentImage = (ImageView)gui.getGUINode(CSG_FAVICON_IMAGE);
            Pane parent = (Pane)currentImage.getParent();
            controller.processChangeSiteImage(parent, currentImage, 
                    DataProperties.faviconPathText.getValue(), CSG_FAVICON_IMAGE,
                    app.getGUIModule().getNodesBuilder(), CLASS_CSG_PANE);
            ImageView favicon = (ImageView)gui.getGUINode(CSG_FAVICON_IMAGE);
            favicon.setFitHeight(50);
            favicon.setFitWidth(50);
        }
        
        if((DataProperties.navbarPathText.getValue() != null && !DataProperties.navbarPathText.getValue().trim().equals(""))) {
            ImageView currentImage = (ImageView)gui.getGUINode(CSG_NAVBAR_IMAGE);
            Pane parent = (Pane)currentImage.getParent();
            controller.processChangeSiteImage(parent, currentImage, 
                    DataProperties.navbarPathText.getValue(), CSG_NAVBAR_IMAGE,
                    app.getGUIModule().getNodesBuilder(), CLASS_CSG_PANE);
        }
        
        if((DataProperties.bottomLeftPathText.getValue() != null && !DataProperties.bottomLeftPathText.getValue().trim().equals(""))) {
            ImageView currentImage = (ImageView)gui.getGUINode(CSG_LEFT_FOOTER_IMAGE);
            Pane parent = (Pane)currentImage.getParent();
            controller.processChangeSiteImage(parent, currentImage, 
                    DataProperties.bottomLeftPathText.getValue(), CSG_LEFT_FOOTER_IMAGE,
                    app.getGUIModule().getNodesBuilder(), CLASS_CSG_PANE);
        }
        
        if((DataProperties.bottomRightPathText.getValue() != null && !DataProperties.bottomRightPathText.getValue().trim().equals(""))) {
            ImageView currentImage = (ImageView)gui.getGUINode(CSG_RIGHT_FOOTER_IMAGE);
            Pane parent = (Pane)currentImage.getParent();
            controller.processChangeSiteImage(parent, currentImage, 
                    DataProperties.bottomRightPathText.getValue(), CSG_RIGHT_FOOTER_IMAGE,
                    app.getGUIModule().getNodesBuilder(), CLASS_CSG_PANE);
        }
        
        ((TextField)gui.getGUINode(CSG_INSTRUCTOR_NAME_TEXT_FIELD)).setText(DataProperties.instructorNameText.getValue());
         DataProperties.instructorNameText.bindBidirectional(((TextField)gui.getGUINode(CSG_INSTRUCTOR_NAME_TEXT_FIELD)).textProperty());
//        
        ((TextField)gui.getGUINode(CSG_INSTRUCTOR_EMAIL_TEXT_FIELD)).setText(DataProperties.instructorEmailText.getValue());
        DataProperties.instructorEmailText.bindBidirectional(((TextField)gui.getGUINode(CSG_INSTRUCTOR_EMAIL_TEXT_FIELD)).textProperty());
//        
        ((TextField)gui.getGUINode(CSG_INSTRUCTOR_ROOM_TEXT_FIELD)).setText(DataProperties.instructorRoomText.getValue());
        DataProperties.instructorRoomText.bindBidirectional(((TextField)gui.getGUINode(CSG_INSTRUCTOR_ROOM_TEXT_FIELD)).textProperty());
//        
        ((TextField)gui.getGUINode(CSG_HOME_PAGE_TEXT_FIELD)).setText(DataProperties.instructorHomePageText.getValue());
        DataProperties.instructorHomePageText.bindBidirectional(((TextField)gui.getGUINode(CSG_HOME_PAGE_TEXT_FIELD)).textProperty());
//        
        ohTextAreaTextProperty.setValue(DataProperties.instructorJSONText.getValue());
        ohTextAreaTextProperty.bindBidirectional(DataProperties.instructorJSONText);
//((TextArea)gui.getGUINode(CSG_OFFICE_HOURS_TEXT_AREA)).setText(DataProperties.instructorJSONText.getValue());
        
//         ohTextAreaTextProperty.((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
//            ((CourseSiteGeneratorData)app.getDataComponent()).getInstructorProperty().setValue(ohTextAreaTextProperty.getValue());
//        });
        // Instructor Office Hours Plus Minus Button Functionality
        Button instructorButton = (Button)gui.getGUINode(CSG_INSTRUCTOR_PLUS_MINUS_BUTTON);
        instructorButton.setOnAction(e -> {
            if(instructorButton.getText().equals("+")){
                //Add the text area
                AppNodesBuilder builder = gui.getNodesBuilder();
                Pane parentPane = (Pane)instructorButton.getParent();
                TextArea instructorTextArea = builder.buildTextArea(CSG_OFFICE_HOURS_TEXT_AREA, parentPane, CLASS_CSG_PANE, ENABLED);
//                if(DataProperties.instructorJSONTextToAdd){
//                    instructorTextArea.setText(DataProperties.instructorJSONText.getValue());
//                }
                instructorButton.setText("-");
                instructorTextArea.setText(ohTextAreaTextProperty.getValue());
                ohTextAreaTextProperty = instructorTextArea.textProperty();
                //instructorTextArea.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            //System.out.println(observable + ", " + oldValue + ", " + newValue);
            //System.out.println(((CourseSiteGeneratorData)app.getDataComponent()).getInstructorProperty().getValue());
                //ohTextAreaText = instructorTextArea.getText();
        //});
                //TODO connectToData
            }
            else {
                //Store the text
                String text = ohTextAreaTextProperty.getValue();
                ohTextAreaTextProperty = new SimpleStringProperty();
                ohTextAreaTextProperty.setValue(text);
                //Remove the text area
                TextArea instructorTextArea = (TextArea)gui.getGUINode(CSG_OFFICE_HOURS_TEXT_AREA);
                Pane parentPane = (Pane)instructorTextArea.getParent();
                parentPane.getChildren().remove(instructorTextArea);
                instructorButton.setText("+");
            }
        });
        
        
        
       // <---------------------- FOR SYLLABUS PANE -------------------------------------------->
        
        TableView officeHoursTableView = (TableView) gui.getGUINode(CSG_OFFICE_HOURS_TABLE_VIEW);
        officeHoursTableView.getSelectionModel().setCellSelectionEnabled(true);
        //officeHoursTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        

        officeHoursTableView.setOnMouseClicked(e -> {
            System.out.println(officeHoursTableView.getSelectionModel().getSelectedCells().size());
            controller.processToggleOfficeHours();
        });
        
        //Descriptin button functionality
        Button descriptionButton = (Button) gui.getGUINode(CSG_DESCRIPTION_BUTTON);
        descriptionButton.setOnAction(e -> {
            VBox descriptionPane = (VBox)gui.getGUINode(CSG_DESCRIPTION_PANE);
            //HBox textBox = (HBox) gui.getGUINode(CSG_DESCRIPTION_TEXT_AREA);
            if(descriptionButton.getText().equals("+")){
                AppNodesBuilder builder = gui.getNodesBuilder();
                HBox textBox = builder.buildHBox(CSG_DESCRIPTION_TEXT_BOX, descriptionPane, CLASS_CSG_PANE, ENABLED);
                TextArea descriptionTextArea = builder.buildTextArea(CSG_DESCRIPTION_TEXT_AREA, textBox, CLASS_CSG_PANE, ENABLED);
                descriptionButton.setText("-");
                descriptionTextArea.setText(descriptionTextProperty.getValue());
                descriptionTextProperty = descriptionTextArea.textProperty();
            } else {
                //Store the text
                String text = descriptionTextProperty.getValue();
                descriptionTextProperty = new SimpleStringProperty();
                descriptionTextProperty.setValue(text);
                //Remove the text area
                HBox textBox = (HBox)gui.getGUINode(CSG_DESCRIPTION_TEXT_BOX);
                textBox.getChildren().remove(gui.getGUINode(CSG_DESCRIPTION_TEXT_AREA));
                descriptionPane.getChildren().remove(textBox);
                descriptionButton.setText("+");
            }
        });
        
        //Topics button functionality
        Button topicsButton = (Button) gui.getGUINode(CSG_TOPICS_BUTTON);
        topicsButton.setOnAction(e -> {
            VBox topicsPane = (VBox)gui.getGUINode(CSG_TOPICS_PANE);
            if(topicsButton.getText().equals("+")){
                AppNodesBuilder builder = gui.getNodesBuilder();
                HBox textBox = builder.buildHBox(CSG_TOPICS_TEXT_BOX, topicsPane, CLASS_CSG_PANE, ENABLED);
                TextArea topicsTextArea = builder.buildTextArea(CSG_TOPICS_TEXT_AREA, textBox, CLASS_CSG_PANE, ENABLED);
                topicsButton.setText("-");
                topicsTextArea.setText(topicsTextProperty.getValue());
                topicsTextProperty = topicsTextArea.textProperty();
            } else {
                //Store the text
                String text = topicsTextProperty.getValue();
                topicsTextProperty = new SimpleStringProperty();
                topicsTextProperty.setValue(text);
                //Remove the text area
                HBox textBox = (HBox)gui.getGUINode(CSG_TOPICS_TEXT_BOX);
                textBox.getChildren().remove(gui.getGUINode(CSG_TOPICS_TEXT_AREA));
                topicsPane.getChildren().remove(textBox);
                topicsButton.setText("+");
            }
        });
        
        //Prerequisites button functionality
        Button prerequisitesButton = (Button) gui.getGUINode(CSG_PREREQUISITES_BUTTON);
        prerequisitesButton.setOnAction(e -> {
            VBox prerequisitesPane = (VBox)gui.getGUINode(CSG_PREREQUISITES_PANE);
            if(prerequisitesButton.getText().equals("+")){
                AppNodesBuilder builder = gui.getNodesBuilder();
                HBox textBox = builder.buildHBox(CSG_PREREQUISITES_TEXT_BOX, prerequisitesPane, CLASS_CSG_PANE, ENABLED);
                TextArea prerequisitesTextArea = builder.buildTextArea(CSG_PREREQUISITES_TEXT_AREA, textBox, CLASS_CSG_PANE, ENABLED);
                prerequisitesButton.setText("-");
                prerequisitesTextArea.setText(prerequisitesTextProperty.getValue());
                prerequisitesTextProperty = prerequisitesTextArea.textProperty();
            } else {
                //Store the text
                String text = prerequisitesTextProperty.getValue();
                prerequisitesTextProperty = new SimpleStringProperty();
                prerequisitesTextProperty.setValue(text);
                //Remove the text area
                HBox textBox = (HBox)gui.getGUINode(CSG_PREREQUISITES_TEXT_BOX);
                textBox.getChildren().remove(gui.getGUINode(CSG_PREREQUISITES_TEXT_AREA));
                prerequisitesPane.getChildren().remove(textBox);
                prerequisitesButton.setText("+");
            }
        });
        
        //Outcomes button functionality
        Button outcomesButton = (Button) gui.getGUINode(CSG_OUTCOMES_BUTTON);
        outcomesButton.setOnAction(e -> {
            VBox outcomesPane = (VBox)gui.getGUINode(CSG_OUTCOMES_PANE);
            if(outcomesButton.getText().equals("+")){
                AppNodesBuilder builder = gui.getNodesBuilder();
                HBox textBox = builder.buildHBox(CSG_OUTCOMES_TEXT_BOX, outcomesPane, CLASS_CSG_PANE, ENABLED);
                TextArea outcomesTextArea = builder.buildTextArea(CSG_OUTCOMES_TEXT_AREA, textBox, CLASS_CSG_PANE, ENABLED);
                outcomesButton.setText("-");
                outcomesTextArea.setText(outcomesTextProperty.getValue());
                outcomesTextProperty = outcomesTextArea.textProperty();
            } else {
                //Store the text
                String text = outcomesTextProperty.getValue();
                outcomesTextProperty = new SimpleStringProperty();
                outcomesTextProperty.setValue(text);
                //Remove the text area
                HBox textBox = (HBox)gui.getGUINode(CSG_OUTCOMES_TEXT_BOX);
                textBox.getChildren().remove(gui.getGUINode(CSG_OUTCOMES_TEXT_AREA));
                outcomesPane.getChildren().remove(textBox);
                outcomesButton.setText("+");
            }
        });
        
        //Textbooks button functionality
        Button textbooksButton = (Button) gui.getGUINode(CSG_TEXTBOOKS_BUTTON);
        textbooksButton.setOnAction(e -> {
            VBox textbooksPane = (VBox)gui.getGUINode(CSG_TEXTBOOKS_PANE);
            if(textbooksButton.getText().equals("+")){
                AppNodesBuilder builder = gui.getNodesBuilder();
                HBox textBox = builder.buildHBox(CSG_TEXTBOOKS_TEXT_BOX, textbooksPane, CLASS_CSG_PANE, ENABLED);
                TextArea textbooksTextArea = builder.buildTextArea(CSG_TEXTBOOKS_TEXT_AREA, textBox, CLASS_CSG_PANE, ENABLED);
                textbooksButton.setText("-");
                textbooksTextArea.setText(textbooksTextProperty.getValue());
                textbooksTextProperty = textbooksTextArea.textProperty();
            } else {
                //Store the text
                String text = textbooksTextProperty.getValue();
                textbooksTextProperty = new SimpleStringProperty();
                textbooksTextProperty.setValue(text);
                //Remove the text area
                HBox textBox = (HBox)gui.getGUINode(CSG_TEXTBOOKS_TEXT_BOX);
                textBox.getChildren().remove(gui.getGUINode(CSG_TEXTBOOKS_TEXT_AREA));
                textbooksPane.getChildren().remove(textBox);
                textbooksButton.setText("+");
            }
        });
        
        //Graded Components button functionality
        Button gradedComponentsButton = (Button) gui.getGUINode(CSG_GRADED_COMPONENTS_BUTTON);
        gradedComponentsButton.setOnAction(e -> {
            VBox gradedComponentsPane = (VBox)gui.getGUINode(CSG_GRADED_COMPONENTS_PANE);
            if(gradedComponentsButton.getText().equals("+")){
                AppNodesBuilder builder = gui.getNodesBuilder();
                HBox textBox = builder.buildHBox(CSG_GRADED_COMPONENTS_TEXT_BOX, gradedComponentsPane, CLASS_CSG_PANE, ENABLED);
                TextArea gradedComponentsTextArea = builder.buildTextArea(CSG_GRADED_COMPONENTS_TEXT_AREA, textBox, CLASS_CSG_PANE, ENABLED);
                gradedComponentsButton.setText("-");
                gradedComponentsTextArea.setText(gradedComponentsTextProperty.getValue());
                gradedComponentsTextProperty = gradedComponentsTextArea.textProperty();
            } else {
                //Store the text
                String text = gradedComponentsTextProperty.getValue();
                gradedComponentsTextProperty = new SimpleStringProperty();
                gradedComponentsTextProperty.setValue(text);
                //Remove the text area
                HBox textBox = (HBox)gui.getGUINode(CSG_GRADED_COMPONENTS_TEXT_BOX);
                textBox.getChildren().remove(gui.getGUINode(CSG_GRADED_COMPONENTS_TEXT_AREA));
                gradedComponentsPane.getChildren().remove(textBox);
                gradedComponentsButton.setText("+");
            }
        });
        
        //Grading Note button functionality
        Button gradingNoteButton = (Button) gui.getGUINode(CSG_GRADING_NOTE_BUTTON);
        gradingNoteButton.setOnAction(e -> {
            VBox gradingNotePane = (VBox)gui.getGUINode(CSG_GRADING_NOTE_PANE);
            if(gradingNoteButton.getText().equals("+")){
                AppNodesBuilder builder = gui.getNodesBuilder();
                HBox textBox = builder.buildHBox(CSG_GRADING_NOTE_TEXT_BOX, gradingNotePane, CLASS_CSG_PANE, ENABLED);
                TextArea gradingNoteTextArea = builder.buildTextArea(CSG_GRADING_NOTE_TEXT_AREA, textBox, CLASS_CSG_PANE, ENABLED);
                gradingNoteButton.setText("-");
                gradingNoteTextArea.setText(gradingNotesTextProperty.getValue());
                gradingNotesTextProperty = gradingNoteTextArea.textProperty();
            } else {
                //Store the text
                String text = gradingNotesTextProperty.getValue();
                gradingNotesTextProperty = new SimpleStringProperty();
                gradingNotesTextProperty.setValue(text);
                //Remove the text area
                HBox textBox = (HBox)gui.getGUINode(CSG_GRADING_NOTE_TEXT_BOX);
                textBox.getChildren().remove(gui.getGUINode(CSG_GRADING_NOTE_TEXT_AREA));
                gradingNotePane.getChildren().remove(textBox);
                gradingNoteButton.setText("+");
            }
        });
        
        //Academic Dishonesty button functionality
        Button academicDishonestyButton = (Button) gui.getGUINode(CSG_ACADEMIC_DISHONESTY_BUTTON);
        academicDishonestyButton.setOnAction(e -> {
            VBox academicDishonestyPane = (VBox)gui.getGUINode(CSG_ACADEMIC_DISHONESTY_PANE);
            if(academicDishonestyButton.getText().equals("+")){
                AppNodesBuilder builder = gui.getNodesBuilder();
                HBox textBox = builder.buildHBox(CSG_ACADEMIC_DISHONESTY_TEXT_BOX, academicDishonestyPane, CLASS_CSG_PANE, ENABLED);
                TextArea academicDishonestyTextArea = builder.buildTextArea(CSG_ACADEMIC_DISHONESTY_TEXT_AREA, textBox, CLASS_CSG_PANE, ENABLED);
                academicDishonestyButton.setText("-");
                academicDishonestyTextArea.setText(academicDishonestyTextProperty.getValue());
                academicDishonestyTextProperty = academicDishonestyTextArea.textProperty();
            } else {
                //Store the text
                String text = academicDishonestyTextProperty.getValue();
                academicDishonestyTextProperty = new SimpleStringProperty();
                academicDishonestyTextProperty.setValue(text);
                //Remove the text area
                HBox textBox = (HBox)gui.getGUINode(CSG_ACADEMIC_DISHONESTY_TEXT_BOX);
                textBox.getChildren().remove(gui.getGUINode(CSG_ACADEMIC_DISHONESTY_TEXT_AREA));
                academicDishonestyPane.getChildren().remove(textBox);
                academicDishonestyButton.setText("+");
            }
        });
        
        //Academic Dishonesty button functionality
        Button specialAssistanceButton = (Button) gui.getGUINode(CSG_SPECIAL_ASSISTANCE_BUTTON);
        specialAssistanceButton.setOnAction((ActionEvent e) -> {
            VBox specialAssistancePane = (VBox)gui.getGUINode(CSG_SPECIAL_ASSISTANCE_PANE);
            if(specialAssistanceButton.getText().equals("+")){
                AppNodesBuilder builder = gui.getNodesBuilder();
                HBox textBox = builder.buildHBox(CSG_SPECIAL_ASSISTANCE_TEXT_BOX, specialAssistancePane, CLASS_CSG_PANE, ENABLED);
                TextArea specialAssistanceTextArea = builder.buildTextArea(CSG_SPECIAL_ASSISTANCE_TEXT_AREA, textBox, CLASS_CSG_PANE, ENABLED);
                specialAssistanceButton.setText("-");
                specialAssistanceButton.setText(specialAssistanceTextProperty.getValue());
                specialAssistanceTextProperty = specialAssistanceTextArea.textProperty();
            } else {
                //Store the text
                String text = specialAssistanceTextProperty.getValue();
                specialAssistanceTextProperty = new SimpleStringProperty();
                specialAssistanceTextProperty.setValue(text);
                //Remove the text area
                HBox textBox = (HBox)gui.getGUINode(CSG_SPECIAL_ASSISTANCE_TEXT_BOX);
                textBox.getChildren().remove(gui.getGUINode(CSG_SPECIAL_ASSISTANCE_TEXT_AREA));
                specialAssistancePane.getChildren().remove(textBox);
                specialAssistanceButton.setText("+");
            }
        });
        
        //For constant update
        new AnimationTimer() 
            {
                PropertiesManager props = PropertiesManager.getPropertiesManager();
                ComboBox typeCB = (ComboBox) gui.getGUINode(CSG_SCHEDULE_TYPE_COMBO_BOX);
                ComboBox semCB = (ComboBox) gui.getGUINode(CSG_SEMESTER_COMBO_BOX);
                TabPane mainTabPane = (TabPane) gui.getGUINode(CSG_MAIN_TAB_PANE);
                Label taDialogName = (Label) gui.getGUINode(CSG_TA_DIALOG_NAME_LABEL);
                Label taDialogEmail = (Label) gui.getGUINode(CSG_TA_DIALOG_EMAIL_LABEL);
            public void handle(MouseEvent e) {
                
                controller.processUpdateExportDirectory();
                
                //Update everything that used the get property method for
                //visual purposes
                typeCB.getItems().set(0,props.getProperty("CSG_HOLIDAY_TEXT"));
                typeCB.getItems().set(1,props.getProperty("CSG_LECTURE_TEXT"));
                typeCB.getItems().set(2,props.getProperty("CSG_RECITATION_TEXT"));
                typeCB.getItems().set(3,props.getProperty("CSG_LAB_TEXT"));
                typeCB.getItems().set(4,props.getProperty("CSG_EXAM_TEXT"));
                typeCB.getItems().set(5,props.getProperty("CSG_HW_TEXT"));
                
                semCB.getItems().set(0,props.getProperty("CSG_FALL_TEXT"));
                semCB.getItems().set(1,props.getProperty("CSG_SPRING_TEXT"));
                semCB.getItems().set(2,props.getProperty("CSG_WINTER_TEXT"));
                semCB.getItems().set(3,props.getProperty("CSG_SUMMER_TEXT"));
                
                mainTabPane.getTabs().get(0).setText(props.getProperty("CSG_SITE_TEXT"));
                mainTabPane.getTabs().get(1).setText(props.getProperty("CSG_SYLLABUS_TEXT"));
                mainTabPane.getTabs().get(2).setText(props.getProperty("CSG_MEETING_TIMES_TEXT"));
                mainTabPane.getTabs().get(3).setText(props.getProperty("CSG_OFFICE_HOURS_TEXT"));
                mainTabPane.getTabs().get(4).setText(props.getProperty("CSG_SCHEDULE_TEXT"));  
                
                
                TableView officeHoursTableView = (TableView) gui.getGUINode(CSG_OFFICE_HOURS_TABLE_VIEW);
             //   officeHoursTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                officeHoursTableView.getSelectionModel().setCellSelectionEnabled(true);
            //    officeHoursTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                app.getFoolproofModule().updateControls(CSG_FOOLPROOF_SETTINGS);
                
            officeHoursTableView = (TableView) gui.getGUINode(CSG_OFFICE_HOURS_TABLE_VIEW);
            
        //    officeHoursTableView.getSelectionModel().setCellSelectionEnabled(true);
         //   officeHoursTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
         //   officeHoursTableView.getSelectionModel().setCellSelectionEnabled(true);
          //  officeHoursTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            }

            @Override
            public void handle(long now) {
                TableView officeHoursTableView = (TableView) gui.getGUINode(CSG_OFFICE_HOURS_TABLE_VIEW);
                officeHoursTableView.getSelectionModel().setCellSelectionEnabled(true);
                controller.processUpdateExportDirectory();
                typeCB.getItems().set(0,props.getProperty("CSG_HOLIDAY_TEXT"));
                typeCB.getItems().set(1,props.getProperty("CSG_LECTURE_TEXT"));
                typeCB.getItems().set(2,props.getProperty("CSG_RECITATION_TEXT"));
                typeCB.getItems().set(3,props.getProperty("CSG_LAB_TEXT"));
                typeCB.getItems().set(4,props.getProperty("CSG_EXAM_TEXT"));
                typeCB.getItems().set(5,props.getProperty("CSG_HW_TEXT"));
                
                semCB.getItems().set(0,props.getProperty("CSG_FALL_TEXT"));
                semCB.getItems().set(1,props.getProperty("CSG_SPRING_TEXT"));
                semCB.getItems().set(2,props.getProperty("CSG_WINTER_TEXT"));
                semCB.getItems().set(3,props.getProperty("CSG_SUMMER_TEXT"));
                
                mainTabPane.getTabs().get(0).setText(props.getProperty("CSG_SITE_TEXT"));
                mainTabPane.getTabs().get(1).setText(props.getProperty("CSG_SYLLABUS_TEXT"));
                mainTabPane.getTabs().get(2).setText(props.getProperty("CSG_MEETING_TIMES_TEXT"));
                mainTabPane.getTabs().get(3).setText(props.getProperty("CSG_OFFICE_HOURS_TEXT"));
                mainTabPane.getTabs().get(4).setText(props.getProperty("CSG_SCHEDULE_TEXT"));
                
                String startTime = (String)(((ComboBox)gui.getGUINode(CSG_START_TIME_COMBO_BOX)).getSelectionModel().getSelectedItem());
                String endTime = (String)(((ComboBox)gui.getGUINode(CSG_END_TIME_COMBO_BOX)).getSelectionModel().getSelectedItem());
                CourseSiteGeneratorData data = (CourseSiteGeneratorData)app.getDataComponent();
                if((startTime != null && endTime != null)
                        && (Integer.parseInt(startTime.substring(0, startTime.indexOf(":"))) != data.getStartHour()
                        || Integer.parseInt(endTime.substring(0, endTime.indexOf(":"))) != data.getEndHour())){
                    data.initHours(data.timeToHour(startTime) + "", data.timeToHour(endTime) + "");
                    app.getFoolproofModule().updateControls(CSG_FOOLPROOF_SETTINGS);
                    data.updateOfficeHours();
                    ((TableView)gui.getGUINode(CSG_OFFICE_HOURS_TABLE_VIEW)).refresh();
                }
                
                ComboBox subjectBox = (ComboBox)gui.getGUINode(CSG_SUBJECT_COMBO_BOX);
                if(!subjectBox.isFocused() && !subjects.contains((String)subjectBox.getSelectionModel().getSelectedItem())
                        && ((String)subjectBox.getSelectionModel().getSelectedItem()).trim() != ""){
                    subjects.add((String)subjectBox.getSelectionModel().getSelectedItem());
                    subjectWriter.write("," + (String)subjectBox.getSelectionModel().getSelectedItem());
                    subjectWriter.flush();
                    
                    subjectBox.getItems().add((String)subjectBox.getSelectionModel().getSelectedItem());
                }
                DataProperties.subjectText.setValue((String)subjectBox.getSelectionModel().getSelectedItem());
                
                
                ComboBox numberBox = (ComboBox)gui.getGUINode(CSG_NUMBER_COMBO_BOX);
                if(!numberBox.isFocused() && !numbers.contains((String)numberBox.getSelectionModel().getSelectedItem())
                        && ((String)numberBox.getSelectionModel().getSelectedItem()).trim() != "") {
                    numbers.add((String)numberBox.getSelectionModel().getSelectedItem());
                    numberWriter.write("," + (String)numberBox.getSelectionModel().getSelectedItem());
                    numberWriter.flush();
                    
                    numberBox.getItems().add((String)numberBox.getSelectionModel().getSelectedItem());
                }
                DataProperties.numberText.setValue((String)numberBox.getSelectionModel().getSelectedItem());
                
               // TableView officeHoursTableView = (TableView) gui.getGUINode(CSG_OFFICE_HOURS_TABLE_VIEW);
             //   officeHoursTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                officeHoursTableView.getSelectionModel().setCellSelectionEnabled(true);
             //
             
           //  officeHoursTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
                app.getFoolproofModule().updateControls(CSG_FOOLPROOF_SETTINGS);
                
          //  TableView officeHoursTableView = (TableView) gui.getGUINode(CSG_OFFICE_HOURS_TABLE_VIEW);
           // officeHoursTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
           // officeHoursTableView.getSelectionModel().setCellSelectionEnabled(true);
           // officeHoursTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            }
            
        }.start();
        
//        gui.getGUINode(CSG_START_TIME_COMBO_BOX).setOnMouseClicked(e -> {
//            String startTime = (String)(((ComboBox)gui.getGUINode(CSG_START_TIME_COMBO_BOX)).getSelectionModel().getSelectedItem());
//            String endTime = (String)(((ComboBox)gui.getGUINode(CSG_END_TIME_COMBO_BOX)).getSelectionModel().getSelectedItem());
//            
//            if(startTime != null && endTime != null) {
//                data.initHours((data.timeToHour(startTime) + ""), (data.timeToHour(endTime) + ""));
//                app.getFoolproofModule().updateControls(CSG_FOOLPROOF_SETTINGS);
//                data.updateOfficeHours();
//                ((TableView)gui.getGUINode(CSG_OFFICE_HOURS_TABLE_VIEW)).refresh();
//            }
//            //((CourseSiteGeneratorData)app.getDataComponent()).updateOfficeHours(startTime, endTime);
//        });
//        
//        gui.getGUINode(CSG_END_TIME_COMBO_BOX).setOnMouseClicked(e -> {
//            String startTime = (String)(((ComboBox)gui.getGUINode(CSG_START_TIME_COMBO_BOX)).getSelectionModel().getSelectedItem());
//            String endTime = (String)(((ComboBox)gui.getGUINode(CSG_END_TIME_COMBO_BOX)).getSelectionModel().getSelectedItem());
//            if(startTime != null && endTime != null) {
//                data.initHours(data.timeToHour(startTime) + "", data.timeToHour(endTime) + "");
//                data.updateOfficeHours();
//                ((TableView)gui.getGUINode(CSG_OFFICE_HOURS_TABLE_VIEW)).refresh();
//            }
            //((CourseSiteGeneratorData)app.getDataComponent()).updateOfficeHours(startTime, endTime);
//        });


        // FOOLPROOF DESIGN STUFF
        TextField nameTextField = ((TextField) gui.getGUINode(CSG_NAME_TEXT_FIELD));
        TextField emailTextField = ((TextField) gui.getGUINode(CSG_EMAIL_TEXT_FIELD));

        nameTextField.textProperty().addListener(e -> {
            controller.processTypeTA();
        });
        emailTextField.textProperty().addListener(e -> {
            controller.processTypeTA();
        });

        // FIRE THE ADD EVENT ACTION
        nameTextField.setOnAction(e -> {
            controller.processAddTA();
        });
        emailTextField.setOnAction(e -> {
            controller.processAddTA();
        });
        
        //SET UP ADD TA BUTTON FUNCTIONALITY
        ((Button) gui.getGUINode(CSG_ADD_TA_BUTTON)).setOnAction(e -> {
            controller.processAddTA();
        });
        
        FileChooser imageChooser = new FileChooser();
        imageChooser.setTitle("Select a logo");
        imageChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        //SET UP FAVICON BUTTON FUNCTIONALITY
        ((Button) gui.getGUINode(CSG_FAVICON_BUTTON)).setOnAction(e -> {
            File selectedFile = imageChooser.showOpenDialog(gui.getGUINode(CSG_SITE_PANE).getScene().getWindow());
            
            HBox box = (HBox)app.getGUIModule().getGUINode(CSG_FAVICON_BOX);
            
            controller.processChangeSiteImage(box, 
                    (ImageView)app.getGUIModule().getGUINode(CSG_FAVICON_IMAGE),
                    selectedFile.getAbsolutePath(), CSG_FAVICON_IMAGE, 
                    app.getGUIModule().getNodesBuilder(), CLASS_CSG_FAVICON_IMAGE);
            ImageView image = (ImageView)app.getGUIModule().getGUINode(CSG_FAVICON_IMAGE);
            image.setFitHeight(50);
            image.setFitWidth(50);
        });
        
        //SET UP NAVBAR BUTTON FUNCTIONALITY
        ((Button) gui.getGUINode(CSG_NAVBAR_BUTTON)).setOnAction(e -> {
            File selectedFile = imageChooser.showOpenDialog(gui.getGUINode(CSG_SITE_PANE).getScene().getWindow());
            
            HBox box = (HBox)app.getGUIModule().getGUINode(CSG_NAVBAR_BOX);
            
            controller.processChangeSiteImage(box, 
                    (ImageView)app.getGUIModule().getGUINode(CSG_NAVBAR_IMAGE),
                    selectedFile.getAbsolutePath(), CSG_NAVBAR_IMAGE, 
                    app.getGUIModule().getNodesBuilder(), CLASS_CSG_PANE);
            ImageView image = (ImageView) app.getGUIModule().getGUINode(CSG_NAVBAR_IMAGE);
        });
        
        //SET UP LEFT FOOTER BUTTON FUNCTIONALITY
        ((Button) gui.getGUINode(CSG_LEFT_FOOTER_BUTTON)).setOnAction(e -> {
            File selectedFile = imageChooser.showOpenDialog(gui.getGUINode(CSG_SITE_PANE).getScene().getWindow());
            
            HBox box = (HBox)app.getGUIModule().getGUINode(CSG_LEFT_FOOTER_BOX);
            
            controller.processChangeSiteImage(box, 
                    (ImageView)app.getGUIModule().getGUINode(CSG_LEFT_FOOTER_IMAGE),
                    selectedFile.getAbsolutePath(), CSG_LEFT_FOOTER_IMAGE, 
                    app.getGUIModule().getNodesBuilder(), CLASS_CSG_PANE);
            ImageView image = (ImageView)app.getGUIModule().getGUINode(CSG_LEFT_FOOTER_IMAGE);
        });
        
        ((Button) gui.getGUINode(CSG_RIGHT_FOOTER_BUTTON)).setOnAction(e -> {
            File selectedFile = imageChooser.showOpenDialog(gui.getGUINode(CSG_SITE_PANE).getScene().getWindow());
            
            HBox box = (HBox)app.getGUIModule().getGUINode(CSG_RIGHT_FOOTER_BOX);
            
            controller.processChangeSiteImage(box, 
                    (ImageView)app.getGUIModule().getGUINode(CSG_RIGHT_FOOTER_IMAGE),
                    selectedFile.getAbsolutePath(), CSG_RIGHT_FOOTER_IMAGE, 
                    app.getGUIModule().getNodesBuilder(), CLASS_CSG_PANE);
            ImageView image = (ImageView)app.getGUIModule().getGUINode(CSG_RIGHT_FOOTER_IMAGE);
        });
        
        // SET UP REMOVE TA FUNCTIONALITY
        ((Button) gui.getGUINode(CSG_TA_MINUS_BUTTON)).setOnAction(e -> {
            TableView taTable = (TableView)gui.getGUINode(CSG_TAS_TABLE_VIEW);
            if(taTable.getSelectionModel().getSelectedItems().size() > 0) {
                TeachingAssistantPrototype ta = (TeachingAssistantPrototype)taTable.getSelectionModel().getSelectedItems().get(0);
                
                ((CourseSiteGeneratorData)app.getDataComponent()).removeTA(ta);
            }
        });

        

        // DON'T LET ANYONE SORT THE TABLES
        TableView tasTableView = (TableView) gui.getGUINode(CSG_TAS_TABLE_VIEW);
        for (int i = 0; i < officeHoursTableView.getColumns().size(); i++) {
            ((TableColumn) officeHoursTableView.getColumns().get(i)).setSortable(false);
        }
        for (int i = 0; i < tasTableView.getColumns().size(); i++) {
            ((TableColumn) tasTableView.getColumns().get(i)).setSortable(false);
        }

        tasTableView.setOnMouseClicked(e -> {
            app.getFoolproofModule().updateAll();
            if (e.getClickCount() == 2) {
                controller.processEditTA();
            }
            controller.processSelectTA();
        });

        //SET UP RADIO BUTTON FUNCTIONALITY
        RadioButton allRadio = (RadioButton) gui.getGUINode(CSG_ALL_RADIO_BUTTON);
        allRadio.setOnAction(e -> {
            controller.processSelectAllTAs();
        });
        RadioButton gradRadio = (RadioButton) gui.getGUINode(CSG_GRADUATE_RADIO_BUTTON);
        gradRadio.setOnAction(e -> {
            controller.processSelectGradTAs();
        });
        RadioButton undergradRadio = (RadioButton) gui.getGUINode(CSG_UNDERGRADUATE_RADIO_BUTTON);
        undergradRadio.setOnAction(e -> {
            controller.processSelectUndergradTAs();
        });
        
            officeHoursTableView.getSelectionModel().setCellSelectionEnabled(true);
    }

    public void initFoolproofDesign() {
        AppGUIModule gui = app.getGUIModule();
        AppFoolproofModule foolproofSettings = app.getFoolproofModule();
        foolproofSettings.registerModeSettings(CSG_FOOLPROOF_SETTINGS,
                new CourseSiteGeneratorFoolproofDesign((CourseSiteGeneratorApp) app));
    }

    @Override
    public void processWorkspaceKeyEvent(KeyEvent ke) {
        // WE AREN'T USING THIS FOR THIS APPLICATION
    }

    @Override
    public void showNewDialog() {
        // WE AREN'T USING THIS FOR THIS APPLICATION
    }
}
