package csg.transactions;

import jtps.jTPS_Transaction;
import csg.CourseSiteGeneratorApp;
import csg.data.CourseSiteGeneratorData;
import csg.data.TeachingAssistantPrototype;

public class PasteTA_Transaction implements jTPS_Transaction {
    CourseSiteGeneratorApp app;
    TeachingAssistantPrototype taToPaste;

    public PasteTA_Transaction(  CourseSiteGeneratorApp initApp, 
                                 TeachingAssistantPrototype initTAToPaste) {
        app = initApp;
        taToPaste = initTAToPaste;
    }

    @Override
    public void doTransaction() {
        CourseSiteGeneratorData data = (CourseSiteGeneratorData)app.getDataComponent();
        data.addTA(taToPaste);
    }

    @Override
    public void undoTransaction() {
        CourseSiteGeneratorData data = (CourseSiteGeneratorData)app.getDataComponent();
        data.removeTA(taToPaste);
    }   
}