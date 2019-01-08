package csg.files;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import djf.components.AppDataComponent;
import djf.components.AppFileComponent;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import csg.CourseSiteGeneratorApp;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_FAVICON_IMAGE;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_HOME_CHECK_BOX;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_HWS_CHECK_BOX;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_LEFT_FOOTER_IMAGE;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_NAVBAR_IMAGE;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_RIGHT_FOOTER_IMAGE;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_SCHEDULE_CHECK_BOX;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_SUBJECT_COMBO_BOX;
import static csg.CourseSiteGeneratorSitePropertyType.CSG_SYLLABUS_CHECK_BOX;
import csg.data.CourseSiteGeneratorData;
import csg.data.DataProperties;
import csg.data.TAType;
import csg.data.TeachingAssistantPrototype;
import csg.data.TimeSlot;
import csg.data.TimeSlot.DayOfWeek;
import csg.workspace.controllers.CourseSiteGeneratorController;
import static csg.workspace.style.CSGStyle.CLASS_CSG_PANE;
import djf.modules.AppGUIModule;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

/**
 * This class serves as the file component for the TA
 * manager app. It provides all saving and loading 
 * services for the application.
 * 
 * @author Richard McKenna
 */
public class CourseSiteGeneratorFiles implements AppFileComponent {
    // THIS IS THE APP ITSELF
    CourseSiteGeneratorApp app;
    
    // THESE ARE USED FOR IDENTIFYING JSON TYPES
    static final String JSON_GRAD_TAS = "grad_tas";
    static final String JSON_UNDERGRAD_TAS = "undergrad_tas";
    static final String JSON_INSTRUCTOR = "instructor";
    static final String JSON_NAME = "name";
    static final String JSON_EMAIL = "email";
    static final String JSON_TYPE = "type";
    static final String JSON_LINK = "link";
    static final String JSON_ROOM = "room";
    static final String JSON_PHOTO = "photo";
    static final String JSON_HOURS = "hours";
    static final String JSON_DAY = "day";
    static final String JSON_TIME = "time";
    static final String JSON_OFFICE_HOURS = "officeHours";
    static final String JSON_START_HOUR = "startHour";
    static final String JSON_END_HOUR = "endHour";
    static final String JSON_START_TIME = "time";
    static final String JSON_DAY_OF_WEEK = "day";
    static final String JSON_MONDAY = "monday";
    static final String JSON_TUESDAY = "tuesday";
    static final String JSON_WEDNESDAY = "wednesday";
    static final String JSON_THURSDAY = "thursday";
    static final String JSON_FRIDAY = "friday";
    static final String JSON_SUBJECT = "subject";
    static final String JSON_NUMBER = "number";
    static final String JSON_SEMESTER = "semester";
    static final String JSON_YEAR = "year";
    static final String JSON_TITLE = "title";
    static final String JSON_LOGOS = "logos";
    static final String JSON_FAVICON = "favicon";
    static final String JSON_NAVBAR = "navbar";
    static final String JSON_BOTTOM_LEFT = "bottom_left";
    static final String JSON_BOTTOM_RIGHT = "bottom_right";
    static final String JSON_SRC = "src";
    static final String JSON_PAGES = "pages";

    public CourseSiteGeneratorFiles(CourseSiteGeneratorApp initApp) {
        app = initApp;
    }

    @Override
    public void loadData(AppDataComponent data, String filePath) throws IOException {
	// CLEAR THE OLD DATA OUT
	CourseSiteGeneratorData dataManager = (CourseSiteGeneratorData)data;
        dataManager.reset();
        DataProperties.reset();

	// LOAD THE JSON FILE WITH ALL THE DATA
	JsonObject json = loadJSONFile(filePath);

	// LOAD THE START AND END HOURS
	String startHour = json.getString(JSON_START_HOUR);
        String endHour = json.getString(JSON_END_HOUR);
        dataManager.initHours(startHour, endHour);
        
        // LOAD THE INSTRUCTOR
        loadInstructor(dataManager, json, JSON_INSTRUCTOR);
        
        // LOAD ALL THE GRAD TAs
        loadGradTAs(dataManager, json, JSON_GRAD_TAS);
        // LOAD ALL THE UNDERGRAD TAs
        loadUndergradTAs(dataManager, json, JSON_UNDERGRAD_TAS);

        // AND THEN ALL THE OFFICE HOURS
        JsonArray jsonOfficeHoursArray = json.getJsonArray(JSON_OFFICE_HOURS);
        for (int i = 0; i < jsonOfficeHoursArray.size(); i++) {
            JsonObject jsonOfficeHours = jsonOfficeHoursArray.getJsonObject(i);
            String startTime = jsonOfficeHours.getString(JSON_START_TIME);
            DayOfWeek dow = DayOfWeek.valueOf(jsonOfficeHours.getString(JSON_DAY_OF_WEEK));
            String name = jsonOfficeHours.getString(JSON_NAME);
            if(dataManager.containsTAWithName(name)){
                TeachingAssistantPrototype ta = dataManager.getTAWithName(name);
                TimeSlot timeSlot = dataManager.getTimeSlot(startTime);
                timeSlot.toggleTA(dow, ta);
            }
        }
        String subject = json.getString(JSON_SUBJECT);
        String number = json.getString(JSON_NUMBER);
        String semester = json.getString(JSON_SEMESTER);
        String year = json.getString(JSON_YEAR);
        String title = json.getString(JSON_TITLE);
        
        //Store Strings in DataProperties
        DataProperties.subjectText.setValue(subject);
        DataProperties.numberText.setValue(number);
        DataProperties.semesterText.setValue(semester);
        DataProperties.yearText.setValue(year);
        DataProperties.titleText.setValue(title);
        loadLogos(dataManager, json, JSON_LOGOS);
        loadPages(dataManager, json, JSON_PAGES);
        
        if(((AppGUIModule)app.getGUIModule()) != null) {
            ((ComboBox)app.getGUIModule().getGUINode(CSG_SUBJECT_COMBO_BOX)).getSelectionModel().select(subject);
            if((DataProperties.faviconPathText.getValue() != null && !DataProperties.faviconPathText.getValue().trim().equals(""))){
                ImageView currentImage = (ImageView)app.getGUIModule().getGUINode(CSG_FAVICON_IMAGE);
                Pane parent = (Pane)currentImage.getParent();
                CourseSiteGeneratorController c = new CourseSiteGeneratorController(app);
                c.processChangeSiteImage(parent, currentImage, 
                    DataProperties.faviconPathText.getValue(), CSG_FAVICON_IMAGE,
                    app.getGUIModule().getNodesBuilder(), CLASS_CSG_PANE);
                ImageView favicon = (ImageView)app.getGUIModule().getGUINode(CSG_FAVICON_IMAGE);
                favicon.setFitHeight(50);
                favicon.setFitWidth(50);
            }
            
            if((DataProperties.navbarPathText.getValue() != null && !DataProperties.navbarPathText.getValue().trim().equals(""))){
                ImageView currentImage = (ImageView)app.getGUIModule().getGUINode(CSG_NAVBAR_IMAGE);
                Pane parent = (Pane)currentImage.getParent();
                CourseSiteGeneratorController c = new CourseSiteGeneratorController(app);
                c.processChangeSiteImage(parent, currentImage, 
                    DataProperties.navbarPathText.getValue(), CSG_NAVBAR_IMAGE,
                    app.getGUIModule().getNodesBuilder(), CLASS_CSG_PANE);
                ImageView navbar = (ImageView)app.getGUIModule().getGUINode(CSG_NAVBAR_IMAGE);
            }
            
            if((DataProperties.bottomLeftPathText.getValue() != null && !DataProperties.bottomLeftPathText.getValue().trim().equals(""))){
                ImageView currentImage = (ImageView)app.getGUIModule().getGUINode(CSG_LEFT_FOOTER_IMAGE);
                Pane parent = (Pane)currentImage.getParent();
                CourseSiteGeneratorController c = new CourseSiteGeneratorController(app);
                c.processChangeSiteImage(parent, currentImage, 
                    DataProperties.bottomLeftPathText.getValue(), CSG_LEFT_FOOTER_IMAGE,
                    app.getGUIModule().getNodesBuilder(), CLASS_CSG_PANE);
                ImageView bottomLeft = (ImageView)app.getGUIModule().getGUINode(CSG_LEFT_FOOTER_IMAGE);
            }
            
            if((DataProperties.bottomRightPathText.getValue() != null && !DataProperties.bottomRightPathText.getValue().trim().equals(""))){
                ImageView currentImage = (ImageView)app.getGUIModule().getGUINode(CSG_RIGHT_FOOTER_IMAGE);
                Pane parent = (Pane)currentImage.getParent();
                CourseSiteGeneratorController c = new CourseSiteGeneratorController(app);
                c.processChangeSiteImage(parent, currentImage, 
                    DataProperties.bottomRightPathText.getValue(), CSG_RIGHT_FOOTER_IMAGE,
                    app.getGUIModule().getNodesBuilder(), CLASS_CSG_PANE);
                ImageView bottomRight = (ImageView)app.getGUIModule().getGUINode(CSG_RIGHT_FOOTER_IMAGE);
            }
            
            if(DataProperties.checkHomePage){
                CheckBox checkBox = (CheckBox)app.getGUIModule().getGUINode(CSG_HOME_CHECK_BOX);
                checkBox.setSelected(true);
            }
            
            if(DataProperties.checkSyllabusPage){
                CheckBox checkBox = (CheckBox)app.getGUIModule().getGUINode(CSG_SYLLABUS_CHECK_BOX);
                checkBox.setSelected(true);
            }
            
            if(DataProperties.checkSchedulePage){
                CheckBox checkBox = (CheckBox)app.getGUIModule().getGUINode(CSG_SCHEDULE_CHECK_BOX);
                checkBox.setSelected(true);
            }
            
            if(DataProperties.checkHWsPage){
                CheckBox checkBox = (CheckBox)app.getGUIModule().getGUINode(CSG_HWS_CHECK_BOX);
                checkBox.setSelected(true);
            }
        
        }
        
        
    }
      
    // HELPER METHOD FOR LOADING DATA FROM A JSON FORMAT
    private JsonObject loadJSONFile(String jsonFilePath) throws IOException {
	InputStream is = new FileInputStream(jsonFilePath);
	JsonReader jsonReader = Json.createReader(is);
	JsonObject json = jsonReader.readObject();
	jsonReader.close();
	is.close();
	return json;
    }
    
    private void loadGradTAs(CourseSiteGeneratorData data, JsonObject json, String tas){
        JsonArray jsonTAArray = json.getJsonArray(tas);
        for(int i = 0; i < jsonTAArray.size(); i++){
            JsonObject jsonTA = jsonTAArray.getJsonObject(i);
            String name = jsonTA.getString(JSON_NAME);
            String email = jsonTA.getString(JSON_EMAIL);
            TeachingAssistantPrototype ta = new TeachingAssistantPrototype(name, email, TAType.Graduate);
            data.addTA(ta);
        }
    }
    
    private void loadUndergradTAs(CourseSiteGeneratorData data, JsonObject json, String tas){
        JsonArray jsonTAArray = json.getJsonArray(tas);
        for(int i = 0; i < jsonTAArray.size(); i++){
            JsonObject jsonTA = jsonTAArray.getJsonObject(i);
            String name = jsonTA.getString(JSON_NAME);
            String email = jsonTA.getString(JSON_EMAIL);
            TeachingAssistantPrototype ta = new TeachingAssistantPrototype(name, email, TAType.Undergraduate);
            data.addTA(ta);
        }
    }
    
    private void loadInstructor(CourseSiteGeneratorData data, JsonObject json, String instructor){
        JsonObject jsonInstructor = json.getJsonObject(instructor);
        String name = jsonInstructor.getString(JSON_NAME);
        String homePage = jsonInstructor.getString(JSON_LINK);
        String email = jsonInstructor.getString(JSON_EMAIL);
        String room = jsonInstructor.getString(JSON_ROOM);
        jsonInstructor.getString(JSON_PHOTO);
        JsonArray jsonHoursArray = jsonInstructor.getJsonArray(JSON_HOURS);
        String jsonHours = "[\n";
        for(int i = 0; i < jsonHoursArray.size(); i++) {
            JsonObject jsonHour = jsonHoursArray.getJsonObject(i);
            String day = jsonHour.getString(JSON_DAY);
            String time = jsonHour.getString(JSON_TIME);
            jsonHours += "    { \"day\": \"" + day + "\",      \"time\": \"" + time + "\"    },\n";
        }
        jsonHours += "]";
        DataProperties.instructorNameText.setValue(name);
        DataProperties.instructorHomePageText.setValue(homePage);
        DataProperties.instructorEmailText.setValue(email);
        DataProperties.instructorRoomText.setValue(room);
        DataProperties.instructorJSONText.setValue(jsonHours);
    }
    
    private void loadLogos(CourseSiteGeneratorData data, JsonObject json, String logos) {
        JsonObject jsonLogos = json.getJsonObject(logos);
        JsonObject jsonFavicon = jsonLogos.getJsonObject(JSON_FAVICON);
        String faviconPath = jsonFavicon.getString(JSON_SRC);
        JsonObject jsonNavbar = jsonLogos.getJsonObject(JSON_NAVBAR);
        String navbarPath = jsonNavbar.getString(JSON_SRC);
        JsonObject jsonBottomLeft = jsonLogos.getJsonObject(JSON_BOTTOM_LEFT);
        String bottomLeftPath = jsonBottomLeft.getString(JSON_SRC);
        JsonObject jsonBottomRight = jsonLogos.getJsonObject(JSON_BOTTOM_RIGHT);
        String bottomRightPath = jsonBottomRight.getString(JSON_SRC);
        
        //Store the paths in dataProperties
        DataProperties.faviconPathText.setValue(faviconPath);
        DataProperties.navbarPathText.setValue(navbarPath);
        DataProperties.bottomLeftPathText.setValue(bottomLeftPath);
        DataProperties.bottomRightPathText.setValue(bottomRightPath);
    }
    
    private void loadPages(CourseSiteGeneratorData data, JsonObject json, String pages){
        JsonArray jsonPageArray = json.getJsonArray(pages);
        for(int i = 0; i < jsonPageArray.size(); i++){
            JsonObject jsonTA = jsonPageArray.getJsonObject(i);
            String name = jsonTA.getString(JSON_NAME);
            switch(name.toUpperCase()){
                case("HOME")     : DataProperties.checkHomePage = true; break;
                case("SYLLABUS") : DataProperties.checkSyllabusPage = true; break;
                case("SCHEDULE")  : DataProperties.checkSchedulePage = true; break;
                case("HWS")      : DataProperties.checkHWsPage = true; break;
            }
        }
    }

    @Override
    public void saveData(AppDataComponent data, String filePath) throws IOException {
	// GET THE DATA
	CourseSiteGeneratorData dataManager = (CourseSiteGeneratorData)data;

	// NOW BUILD THE TA JSON OBJCTS TO SAVE
	JsonArrayBuilder gradTAsArrayBuilder = Json.createArrayBuilder();
        JsonArrayBuilder undergradTAsArrayBuilder = Json.createArrayBuilder();
	Iterator<TeachingAssistantPrototype> tasIterator = dataManager.teachingAssistantsIterator();
        while (tasIterator.hasNext()) {
            TeachingAssistantPrototype ta = tasIterator.next();
	    JsonObject taJson = Json.createObjectBuilder()
		    .add(JSON_NAME, ta.getName())
		    .add(JSON_EMAIL, ta.getEmail())
                    .add(JSON_TYPE, ta.getType().toString()).build();
            if (ta.getType().equals(TAType.Graduate.toString()))
                gradTAsArrayBuilder.add(taJson);
            else
                undergradTAsArrayBuilder.add(taJson);
	}
        JsonArray gradTAsArray = gradTAsArrayBuilder.build();
	JsonArray undergradTAsArray = undergradTAsArrayBuilder.build();

	// NOW BUILD THE OFFICE HOURS JSON OBJCTS TO SAVE
	JsonArrayBuilder officeHoursArrayBuilder = Json.createArrayBuilder();
        Iterator<TimeSlot> timeSlotsIterator = dataManager.officeHoursIterator();
        while (timeSlotsIterator.hasNext()) {
            TimeSlot timeSlot = timeSlotsIterator.next();
            for (int i = 0; i < DayOfWeek.values().length; i++) {
                DayOfWeek dow = DayOfWeek.values()[i];
                tasIterator = timeSlot.getTAsIterator(dow);
                while (tasIterator.hasNext()) {
                    TeachingAssistantPrototype ta = tasIterator.next();
                    JsonObject tsJson = Json.createObjectBuilder()
                        .add(JSON_START_TIME, timeSlot.getStartTime().replace(":", "_"))
                        .add(JSON_DAY_OF_WEEK, dow.toString())
                        .add(JSON_NAME, ta.getName()).build();
                    officeHoursArrayBuilder.add(tsJson);
                }
            }
	}
	JsonArray officeHoursArray = officeHoursArrayBuilder.build();
        
	// THEN PUT IT ALL TOGETHER IN A JsonObject
	JsonObject dataManagerJSO = Json.createObjectBuilder()
		.add(JSON_START_HOUR, "" + dataManager.getStartHour())
		.add(JSON_END_HOUR, "" + dataManager.getEndHour())
                .add(JSON_GRAD_TAS, gradTAsArray)
                .add(JSON_UNDERGRAD_TAS, undergradTAsArray)
                .add(JSON_OFFICE_HOURS, officeHoursArray)
		.build();
	
	// AND NOW OUTPUT IT TO A JSON FILE WITH PRETTY PRINTING
	Map<String, Object> properties = new HashMap<>(1);
	properties.put(JsonGenerator.PRETTY_PRINTING, true);
	JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
	StringWriter sw = new StringWriter();
	JsonWriter jsonWriter = writerFactory.createWriter(sw);
	jsonWriter.writeObject(dataManagerJSO);
	jsonWriter.close();

	// INIT THE WRITER
	OutputStream os = new FileOutputStream(filePath);
	JsonWriter jsonFileWriter = Json.createWriter(os);
	jsonFileWriter.writeObject(dataManagerJSO);
	String prettyPrinted = sw.toString();
	PrintWriter pw = new PrintWriter(filePath);
	pw.write(prettyPrinted);
	pw.close();
    }
    
    // IMPORTING/EXPORTING DATA IS USED WHEN WE READ/WRITE DATA IN AN
    // ADDITIONAL FORMAT USEFUL FOR ANOTHER PURPOSE, LIKE ANOTHER APPLICATION

    @Override
    public void importData(AppDataComponent data, String filePath) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void exportData(AppDataComponent data, String filePath) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}