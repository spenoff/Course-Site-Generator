/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csg.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author spencernisonoff
 */
public class DataProperties {
    //<------------------- DATA RELATED TO THE SITE PANE --------------------->
    public static StringProperty subjectText = new SimpleStringProperty();
    public static StringProperty numberText = new SimpleStringProperty();
    public static StringProperty semesterText = new SimpleStringProperty();
    public static StringProperty yearText = new SimpleStringProperty();
    public static StringProperty titleText = new SimpleStringProperty();
    public static StringProperty instructorJSONText = new SimpleStringProperty();
    public static StringProperty instructorNameText = new SimpleStringProperty();
    public static StringProperty instructorEmailText = new SimpleStringProperty();
    public static StringProperty instructorRoomText = new SimpleStringProperty();
    public static StringProperty instructorHomePageText = new SimpleStringProperty();
    public static boolean checkHomePage = false;
    public static boolean checkSyllabusPage = false;
    public static boolean checkSchedulePage = false;
    public static boolean checkHWsPage = false;
    
           //images
    public static StringProperty faviconPathText = new SimpleStringProperty();
    public static StringProperty navbarPathText = new SimpleStringProperty();
    public static StringProperty bottomLeftPathText = new SimpleStringProperty();
    public static StringProperty bottomRightPathText = new SimpleStringProperty();
    
    //<------------ DATA RELATED TO THE SYLLABUS PANE ----------------->
    public static StringProperty descriptionText = new SimpleStringProperty();
    public static StringProperty topicsText = new SimpleStringProperty();
    public static StringProperty prerequisitesText = new SimpleStringProperty();
    public static StringProperty outcomesText = new SimpleStringProperty();
    public static StringProperty textbooksText = new SimpleStringProperty();
    public static StringProperty gradedComponentsText = new SimpleStringProperty();
    public static StringProperty gradingNoteText = new SimpleStringProperty();
    public static StringProperty academicDishonestyText = new SimpleStringProperty();
    public static StringProperty specialAssistanceText = new SimpleStringProperty();
    
    //<-------------- DATA RELATED TO THE OFFICE HOURS PANE ---------------->
    
    public static void reset(){
        subjectText.setValue("");
        numberText.setValue("");
        semesterText.setValue("");
        yearText.setValue("");
        titleText.setValue("");
        instructorJSONText.setValue("");
        instructorNameText.setValue("");
        instructorEmailText.setValue("");
        instructorRoomText.setValue("");
        instructorHomePageText.setValue("");
        faviconPathText.setValue("");
        navbarPathText.setValue("");
        bottomLeftPathText.setValue("");
        bottomRightPathText.setValue("");
        checkHomePage = false;
        checkSyllabusPage = false;
        checkSchedulePage = false;
        checkHWsPage = false;
        
        //instructorJSONTextToAdd = true;
        descriptionText.setValue("");
        topicsText.setValue("");
        prerequisitesText.setValue("");
        outcomesText.setValue("");
        textbooksText.setValue("");
        gradedComponentsText.setValue("");
        gradingNoteText.setValue("");
        academicDishonestyText.setValue("");
        specialAssistanceText.setValue("");
    }
}
