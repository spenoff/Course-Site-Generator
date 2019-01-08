package csg.transactions;

import jtps.jTPS_Transaction;
import static djf.AppPropertyType.APP_CLIPBOARD_FOOLPROOF_SETTINGS;
import java.util.ArrayList;
import java.util.HashMap;
import csg.CourseSiteGeneratorApp;
import csg.data.CourseSiteGeneratorData;
import csg.data.TeachingAssistantPrototype;
import csg.data.TimeSlot;
import csg.data.TimeSlot.DayOfWeek;

public class CutTA_Transaction implements jTPS_Transaction {
    CourseSiteGeneratorApp app;
    TeachingAssistantPrototype taToCut;
    HashMap<TimeSlot, ArrayList<DayOfWeek>> officeHours;

    public CutTA_Transaction(CourseSiteGeneratorApp initApp, 
            TeachingAssistantPrototype initTAToCut, 
            HashMap<TimeSlot, ArrayList<DayOfWeek>> initOfficeHours) {
        app = initApp;
        taToCut = initTAToCut;
        officeHours = initOfficeHours;
    }

    @Override
    public void doTransaction() {
        CourseSiteGeneratorData data = (CourseSiteGeneratorData)app.getDataComponent();
        data.removeTA(taToCut, officeHours);
        app.getFoolproofModule().updateControls(APP_CLIPBOARD_FOOLPROOF_SETTINGS);
    }

    @Override
    public void undoTransaction() {
        CourseSiteGeneratorData data = (CourseSiteGeneratorData)app.getDataComponent();
        data.addTA(taToCut, officeHours);
        app.getFoolproofModule().updateControls(APP_CLIPBOARD_FOOLPROOF_SETTINGS);
    }   
}