package csg.data;

import javafx.collections.ObservableList;
import djf.components.AppDataComponent;
import djf.modules.AppGUIModule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import csg.CourseSiteGeneratorApp;
import static csg.CourseSiteGeneratorOfficeHoursPropertyType.CSG_ALL_RADIO_BUTTON;
import static csg.CourseSiteGeneratorOfficeHoursPropertyType.CSG_END_TIME_COMBO_BOX;
import static csg.CourseSiteGeneratorOfficeHoursPropertyType.CSG_GRADUATE_RADIO_BUTTON;
import static csg.CourseSiteGeneratorOfficeHoursPropertyType.CSG_OFFICE_HOURS_TABLE_VIEW;
import static csg.CourseSiteGeneratorOfficeHoursPropertyType.CSG_START_TIME_COMBO_BOX;
import static csg.CourseSiteGeneratorOfficeHoursPropertyType.CSG_TAS_TABLE_VIEW;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_FAVICON_IMAGE;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_HOME_CHECK_BOX;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_HWS_CHECK_BOX;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_INSTRUCTOR_NAME_TEXT_FIELD;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_NUMBER_COMBO_BOX;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_OFFICE_HOURS_TEXT_AREA;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_SEMESTER_COMBO_BOX;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_SUBJECT_COMBO_BOX;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_SYLLABUS_CHECK_BOX;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_YEAR_COMBO_BOX;
import csg.data.TimeSlot.DayOfWeek;
import csg.workspace.CourseSiteGeneratorWorkspace;
import java.awt.Image;
import java.io.IOException;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

/**
 * This is the data component for TAManagerApp. It has all the data needed
 * to be set by the user via the User Interface and file I/O can set and get
 * all the data from this object
 * 
 * @author Richard McKenna
 */
public class CourseSiteGeneratorData implements AppDataComponent {

    // WE'LL NEED ACCESS TO THE APP TO NOTIFY THE GUI WHEN DATA CHANGES
    CourseSiteGeneratorApp app;
    
    //<------------ DATA RELATED TO THE OFFICE HOURS PANE ---------------->
    
    // THESE ARE ALL THE TEACHING ASSISTANTS
    HashMap<TAType, ArrayList<TeachingAssistantPrototype>> allTAs = new HashMap();
    // THESE ARE ALL THE TIME SLOTS
    ArrayList<TimeSlot> allOfficeHours = new ArrayList<TimeSlot>();;
    //HashMap<Integer, TimeSlot> allHalfTimeSlots;
    boolean startsWithHalf;
    boolean firstReset = true;

    // NOTE THAT THIS DATA STRUCTURE WILL DIRECTLY STORE THE
    // DATA IN THE ROWS OF THE TABLE VIEW
    ObservableList<TeachingAssistantPrototype> teachingAssistants;
    ObservableList<TimeSlot> officeHours;

    // THESE ARE THE TIME BOUNDS FOR THE OFFICE HOURS GRID. NOTE
    // THAT THESE VALUES CAN BE DIFFERENT FOR DIFFERENT FILES, BUT
    // THAT OUR APPLICATION USES THE DEFAULT TIME VALUES AND PROVIDES
    // NO MEANS FOR CHANGING THESE VALUES
    int startHour;
    int endHour;
    
    // DEFAULT VALUES FOR START AND END HOURS IN MILITARY HOURS
    public static final int MIN_START_HOUR = 9;
    public static final int MAX_END_HOUR = 20;

    /**
     * This constructor will setup the required data structures for
     * use, but will have to wait on the office hours grid, since
     * it receives the StringProperty objects from the Workspace.
     * 
     * @param initApp The application this data manager belongs to. 
     */
    public CourseSiteGeneratorData(CourseSiteGeneratorApp initApp) {
        // KEEP THIS FOR LATER
        app = initApp;
        AppGUIModule gui = app.getGUIModule();
        //DATA RELATED TO SITE PANE
//        instructorJSONText = ((TextArea) gui.getGUINode(CSG_OFFICE_HOURS_TEXT_AREA)).textProperty();
        
        // DATA RELATED TO OFFICE HOURS PANE
        // SETUP THE DATA STRUCTURES
       // allTAs = new HashMap();
        allTAs.put(TAType.Graduate, new ArrayList());
        allTAs.put(TAType.Undergraduate, new ArrayList());
        
        
        // GET THE LIST OF TAs FOR THE LEFT TABLE
        TableView<TeachingAssistantPrototype> taTableView = (TableView)gui.getGUINode(CSG_TAS_TABLE_VIEW);
        teachingAssistants = taTableView.getItems();

        // THESE ARE THE DEFAULT OFFICE HOURS
        startHour = MIN_START_HOUR;
        endHour = MAX_END_HOUR;
        
        resetOfficeHours();
        
        //allTimeSlots = new HashMap<Integer, TimeSlot>();
        //allHalfTimeSlots = new HashMap<Integer, TimeSlot>();
        startsWithHalf = false;
    }
    
    // ACCESSOR METHODS

    public int getStartHour() {
        return startHour;
    }

    public int getEndHour() {
        return endHour;
    }
    
    // PRIVATE HELPER METHODS
    
    private void sortTAs() {
        Collections.sort(teachingAssistants);
    }
    
    private void resetOfficeHours() {
        //THIS WILL STORE OUR OFFICE HOURS
        AppGUIModule gui = app.getGUIModule();
        
        TableView<TimeSlot> officeHoursTableView = (TableView)gui.getGUINode(CSG_OFFICE_HOURS_TABLE_VIEW);
        ComboBox startTimeBox = (ComboBox)gui.getGUINode(CSG_START_TIME_COMBO_BOX);
        ComboBox endTimeBox = (ComboBox)gui.getGUINode(CSG_END_TIME_COMBO_BOX);
        startTimeBox.getSelectionModel().select(hourToTime(startHour));
        endTimeBox.getSelectionModel().select(hourToTime(endHour));
        
        officeHours = officeHoursTableView.getItems(); 
        officeHours.clear();
        //allOfficeHours.clear();
        for (int i = 0; i < startHour; i++){
            TimeSlot timeSlot = new TimeSlot(   this.getTimeString(i, true),
                                                this.getTimeString(i, false));
            allOfficeHours.add(timeSlot);
            
            TimeSlot halfTimeSlot = new TimeSlot(   this.getTimeString(i, false),
                                                    this.getTimeString(i+1, true));
            allOfficeHours.add(halfTimeSlot);
        
        }
        for (int i = startHour; i <= endHour; i++) {
            TimeSlot timeSlot = new TimeSlot(   this.getTimeString(i, true),
                                                this.getTimeString(i, false));
            officeHours.add(timeSlot);
            allOfficeHours.add(timeSlot);
           // allTimeSlots.put(i, timeSlot);
            
            TimeSlot halfTimeSlot = new TimeSlot(   this.getTimeString(i, false),
                                                    this.getTimeString(i+1, true));
            officeHours.add(halfTimeSlot);
            allOfficeHours.add(halfTimeSlot);
        }
        for(int i = endHour+1; i < 20; i++){
            TimeSlot timeSlot = new TimeSlot(   this.getTimeString(i, true),
                                                this.getTimeString(i, false));
            allOfficeHours.add(timeSlot);
            
            TimeSlot halfTimeSlot = new TimeSlot(   this.getTimeString(i, false),
                                                    this.getTimeString(i+1, true));
            allOfficeHours.add(halfTimeSlot);
        }
        officeHoursTableView.getSelectionModel().setCellSelectionEnabled(true);
       // officeHoursTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }
    
    public void updateOfficeHours(){
        officeHours.clear();
        for(int j = startHour*2; j < endHour*2; j++) {
            boolean add = officeHours.add(allOfficeHours.get(j));
        }
    }
    
//    public void updateOfficeHours(String startTime, String endTime) {
//        //TODO check legality with foolproof
//        if(startTime.contains("30")){
//            startsWithHalf = true;
//        } else{
//            startsWithHalf = false;
//        }
//        startHour = 
//        TableView<TimeSlot> officeHoursTableView = (TableView)gui.getGUINode(CSG_OFFICE_HOURS_TABLE_VIEW);
//        officeHours.clear();
//        if(startsWithHalf){
//            for(int i = startTime; i <= endTime; i++){
//                
//            }
//        }
//    }
    
    private String getTimeString(int militaryHour, boolean onHour) {
        String minutesText = "00";
        if (!onHour) {
            minutesText = "30";
        }

        // FIRST THE START AND END CELLS
        int hour = militaryHour;
        if (hour > 12) {
            hour -= 12;
        }
        String cellText = "" + hour + ":" + minutesText;
        if (militaryHour < 12) {
            cellText += "am";
        } else {
            cellText += "pm";
        }
        return cellText;
    }
    
    // METHODS TO OVERRIDE
        
    /**
     * Called each time new work is created or loaded, it resets all data
     * and data structures such that they can be used for new values.
     */
    @Override
    public void reset() {
        AppGUIModule gui = app.getGUIModule();
        // For Site Pane
        if(gui != null) {
            //((ComboBox)gui.getGUINode(CSG_SUBJECT_COMBO_BOX)).getSelectionModel().selectFirst();
            //((ComboBox)gui.getGUINode(CSG_NUMBER_COMBO_BOX)).getSelectionModel().selectFirst();
            ((ComboBox)gui.getGUINode(CSG_SEMESTER_COMBO_BOX)).getSelectionModel().selectFirst();
            ((ComboBox)gui.getGUINode(CSG_YEAR_COMBO_BOX)).getSelectionModel().selectFirst();
            ((CheckBox)gui.getGUINode(CSG_HOME_CHECK_BOX)).setSelected(false);
            ((CheckBox)gui.getGUINode(CSG_SYLLABUS_CHECK_BOX)).setSelected(false);
            ((CheckBox)gui.getGUINode(CSG_HWS_CHECK_BOX)).setSelected(false);
        }
        //TODO fonts and Colors style sheet
        
        
        // For Office Hours Pane
        startHour = MIN_START_HOUR;
        endHour = MAX_END_HOUR;
        teachingAssistants.clear();
        if(!firstReset){
            allTAs.get(TAType.Undergraduate).clear();
            allTAs.get(TAType.Graduate).clear();
        }
        firstReset = false;
        
        for (TimeSlot timeSlot : officeHours) {
            timeSlot.reset();
        }
        for (TimeSlot timeSlot : allOfficeHours) {
            timeSlot.reset();
        }
        DataProperties.reset();
    }
    
    // SERVICE METHODS
    
    public void initHours(String startHourText, String endHourText) {
        int initStartHour = Integer.parseInt(startHourText);
        int initEndHour = Integer.parseInt(endHourText);
        if (initStartHour <= initEndHour) {
            // THESE ARE VALID HOURS SO KEEP THEM
            // NOTE THAT THESE VALUES MUST BE PRE-VERIFIED
            startHour = initStartHour;
            endHour = initEndHour;
        }
        resetOfficeHours();
    }
    
    public void addTA(TeachingAssistantPrototype ta) {
        if (!hasTA(ta)) {
            TAType taType = TAType.valueOf(ta.getType());
            ArrayList<TeachingAssistantPrototype> tas = allTAs.get(taType);
            tas.add(ta);
            this.updateTAs();
        }
    }

    public void addTA(TeachingAssistantPrototype ta, HashMap<TimeSlot, ArrayList<DayOfWeek>> officeHours) {
        addTA(ta);
        for (TimeSlot timeSlot : officeHours.keySet()) {
            ArrayList<DayOfWeek> days = officeHours.get(timeSlot);
            for (DayOfWeek dow : days) {
                timeSlot.addTA(dow, ta);
            }
        }
    }
    
    public void removeTA(TeachingAssistantPrototype ta) {
        // REMOVE THE TA FROM THE LIST OF TAs
        TAType taType = TAType.valueOf(ta.getType());
        allTAs.get(taType).remove(ta);
        
        // REMOVE THE TA FROM ALL OF THEIR OFFICE HOURS
        for (TimeSlot timeSlot : officeHours) {
            timeSlot.removeTA(ta);
        }
        
        // AND REFRESH THE TABLES
        this.updateTAs();
    }

    public void removeTA(TeachingAssistantPrototype ta, HashMap<TimeSlot, ArrayList<DayOfWeek>> officeHours) {
        removeTA(ta);
        for (TimeSlot timeSlot : officeHours.keySet()) {
            ArrayList<DayOfWeek> days = officeHours.get(timeSlot);
            for (DayOfWeek dow : days) {
                timeSlot.removeTA(dow, ta);
            }
        }
    }
    
    public DayOfWeek getColumnDayOfWeek(int columnNumber) {
        return TimeSlot.DayOfWeek.values()[columnNumber-2];
    }

    public TeachingAssistantPrototype getTAWithName(String name) {
        Iterator<TeachingAssistantPrototype> taIterator = teachingAssistants.iterator();
        while (taIterator.hasNext()) {
            TeachingAssistantPrototype ta = taIterator.next();
            if (ta.getName().equals(name))
                return ta;
        }
        return null;
    }

    public TeachingAssistantPrototype getTAWithEmail(String email) {
        Iterator<TeachingAssistantPrototype> taIterator = teachingAssistants.iterator();
        while (taIterator.hasNext()) {
            TeachingAssistantPrototype ta = taIterator.next();
            if (ta.getEmail().equals(email))
                return ta;
        }
        return null;
    }

    public TimeSlot getTimeSlot(String startTime) {
        Iterator<TimeSlot> timeSlotsIterator = officeHours.iterator();
        while (timeSlotsIterator.hasNext()) {
            TimeSlot timeSlot = timeSlotsIterator.next();
            String timeSlotStartTime = timeSlot.getStartTime().replace(":", "_");
            if (timeSlotStartTime.equals(startTime))
                return timeSlot;
        }
        return null;
    }

    public TAType getSelectedType() {
        RadioButton allRadio = (RadioButton)app.getGUIModule().getGUINode(CSG_ALL_RADIO_BUTTON);
        if (allRadio.isSelected())
            return TAType.All;
        RadioButton gradRadio = (RadioButton)app.getGUIModule().getGUINode(CSG_GRADUATE_RADIO_BUTTON);
        if (gradRadio.isSelected())
            return TAType.Graduate;
        else
            return TAType.Undergraduate;
    }

    public TeachingAssistantPrototype getSelectedTA() {
        AppGUIModule gui = app.getGUIModule();
        TableView<TeachingAssistantPrototype> tasTable = (TableView)gui.getGUINode(CSG_TAS_TABLE_VIEW);
        return tasTable.getSelectionModel().getSelectedItem();
    }
    
    public HashMap<TimeSlot, ArrayList<DayOfWeek>> getTATimeSlots(TeachingAssistantPrototype ta) {
        HashMap<TimeSlot, ArrayList<DayOfWeek>> timeSlots = new HashMap();
        for (TimeSlot timeSlot : officeHours) {
            if (timeSlot.hasTA(ta)) {
                ArrayList<DayOfWeek> daysForTA = timeSlot.getDaysForTA(ta);
                timeSlots.put(timeSlot, daysForTA);
            }
        }
        return timeSlots;
    }
    
    private boolean hasTA(TeachingAssistantPrototype testTA) {
        return allTAs.get(TAType.Graduate).contains(testTA)
                ||
                allTAs.get(TAType.Undergraduate).contains(testTA);
    }
    
    public boolean isTASelected() {
        AppGUIModule gui = app.getGUIModule();
        TableView tasTable = (TableView)gui.getGUINode(CSG_TAS_TABLE_VIEW);
        return tasTable.getSelectionModel().getSelectedItem() != null;
    }

    public boolean isLegalNewTA(String name, String email) {
        if ((name.trim().length() > 0)
                && (email.trim().length() > 0)) {
            // MAKE SURE NO TA ALREADY HAS THE SAME NAME
            TAType type = this.getSelectedType();
            TeachingAssistantPrototype testTA = new TeachingAssistantPrototype(name, email, type);
            if (this.teachingAssistants.contains(testTA))
                return false;
            if (this.isLegalNewEmail(email)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isLegalNewName(String testName) {
        if (testName.trim().length() > 0) {
            for (TeachingAssistantPrototype testTA : this.teachingAssistants) {
                if (testTA.getName().equals(testName))
                    return false;
            }
            return true;
        }
        return false;
    }
    
    public boolean isLegalNewEmail(String email) {
        Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile(
                "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(email);
        if (matcher.find()) {
            for (TeachingAssistantPrototype ta : this.teachingAssistants) {
                if (ta.getEmail().equals(email.trim()))
                    return false;
            }
            return true;
        }
        else return false;
    }
    
    public boolean isDayOfWeekColumn(int columnNumber) {
        return columnNumber >= 2;
    }
    
    public boolean isTATypeSelected() {
        AppGUIModule gui = app.getGUIModule();
        RadioButton allRadioButton = (RadioButton)gui.getGUINode(CSG_ALL_RADIO_BUTTON);
        return !allRadioButton.isSelected();
    }
    
    public boolean isValidTAEdit(TeachingAssistantPrototype taToEdit, String name, String email) {
        if (!taToEdit.getName().equals(name)) {
            if (!this.isLegalNewName(name))
                return false;
        }
        if (!taToEdit.getEmail().equals(email)) {
            if (!this.isLegalNewEmail(email))
                return false;
        }
        return true;
    }

    public boolean isValidNameEdit(TeachingAssistantPrototype taToEdit, String name) {
        if (!taToEdit.getName().equals(name)) {
            if (!this.isLegalNewName(name))
                return false;
        }
        return true;
    }

    public boolean isValidEmailEdit(TeachingAssistantPrototype taToEdit, String email) {
        if (!taToEdit.getEmail().equals(email)) {
            if (!this.isLegalNewEmail(email))
                return false;
        }
        return true;
    }    

    public void updateTAs() {
        TAType type = getSelectedType();
        selectTAs(type);
    }
    
    public void selectTAs(TAType type) {
        teachingAssistants.clear();
        Iterator<TeachingAssistantPrototype> tasIt = this.teachingAssistantsIterator();
        while (tasIt.hasNext()) {
            TeachingAssistantPrototype ta = tasIt.next();
            if (type.equals(TAType.All)) {
                teachingAssistants.add(ta);
            }
            else if (ta.getType().equals(type.toString())) {
                teachingAssistants.add(ta);
            }
        }
        
        // SORT THEM BY NAME
        sortTAs();

        // CLEAR ALL THE OFFICE HOURS
        Iterator<TimeSlot> officeHoursIt = officeHours.iterator();
        while (officeHoursIt.hasNext()) {
            TimeSlot timeSlot = officeHoursIt.next();
            timeSlot.filter(type);
        }
        
        app.getFoolproofModule().updateAll();
    }
    
    public Iterator<TimeSlot> officeHoursIterator() {
        return officeHours.iterator();
    }

    public Iterator<TeachingAssistantPrototype> teachingAssistantsIterator() {
        return new AllTAsIterator();
    }
    
    private class AllTAsIterator implements Iterator {
        Iterator gradIt = allTAs.get(TAType.Graduate).iterator();
        Iterator undergradIt = allTAs.get(TAType.Undergraduate).iterator();

        public AllTAsIterator() {}
        
        @Override
        public boolean hasNext() {
            if (gradIt.hasNext() || undergradIt.hasNext())
                return true;
            else
                return false;                
        }

        @Override
        public Object next() {
            if (gradIt.hasNext())
                return gradIt.next();
            else if (undergradIt.hasNext())
                return undergradIt.next();
            else
                return null;
        }
        
        
    }
    public static int timeToHour(String time){
            switch(time){
                case "12:00am" : return 0;
                case "1:00am"  : return 1;
                case "2:00am"  : return 2;
                case "3:00am"  : return 3;
                case "4:00am"  : return 4;
                case "5:00am"  : return 5;
                case "6:00am"  : return 6;
                case "7:00am"  : return 7;
                case "8:00am"  : return 8;
                case "9:00am"  : return 9;
                case "10:00am" : return 10;
                case "11:00am" : return 11;
                case "12:00pm" : return 12;
                case "1:00pm"  : return 13;
                case "2:00pm"  : return 14;
                case "3:00pm"  : return 15;
                case "4:00pm"  : return 16;
                case "5:00pm"  : return 17;
                case "6:00pm"  : return 18;
                case "7:00pm"  : return 19;
                case "8:00pm"  : return 20;
                case "9:00pm"  : return 21;
                case "10:00pm" : return 22;
                case "11:00pm" : return 23;
                default        : return -1;
            }
        }
    
    public static String hourToTime(int hour){
        switch(hour) {
            case 0  : return "12:00am";
            case 1  : return "1:00am";
            case 2  : return "2:00am";
            case 3  : return "3:00am";
            case 4  : return "4:00am";
            case 5  : return "5:00am";
            case 6  : return "6:00am";
            case 7  : return "7:00am";
            case 8  : return "8:00am";
            case 9  : return "9:00am";
            case 10 : return "10:00am";
            case 11 : return "11:00am";
            case 12 : return "12:00pm";
            case 13 : return "1:00pm";
            case 14 : return "2:00pm";
            case 15 : return "3:00pm";
            case 16 : return "4:00pm";
            case 17 : return "5:00pm";
            case 18 : return "6:00pm";
            case 19 : return "7:00pm";
            case 20 : return "8:00pm";
            case 21 : return "9:00pm";
            case 22 : return "10:00pm";
            case 23 : return "11:00pm";
            default : return "Not a time";
        }
    }
    
    public boolean containsTAWithName(String name){
        ArrayList<TeachingAssistantPrototype> gradTAs = allTAs.get(TAType.Graduate);
        ArrayList<TeachingAssistantPrototype> undergradTAs = allTAs.get(TAType.Undergraduate);
        for(TeachingAssistantPrototype ta : gradTAs) {
            if(ta.getName().equals(name)) return true;
        }
        for(TeachingAssistantPrototype ta : undergradTAs) {
            if(ta.getName().equals(name)) return true;
        }
        return false;
    }
    
//    
//    public String instrucotrOHText(){
//        return instructorJSONText.getValue();
//    }
}