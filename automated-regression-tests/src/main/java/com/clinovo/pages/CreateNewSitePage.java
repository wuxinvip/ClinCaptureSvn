package com.clinovo.pages;

import net.thucydides.core.annotations.findby.By;
import net.thucydides.core.annotations.findby.FindBy;
import net.thucydides.core.pages.WebElementFacade;
import net.thucydides.core.webelements.RadioButtonGroup;

import org.openqa.selenium.WebDriver;

import com.clinovo.pages.beans.Study;

public class CreateNewSitePage extends BasePage {

	public static final String PAGE_NAME = "Create a New Site page";
	public static final String PAGE_URL = "CreateSubStudy";
	
	@FindBy(name = "name")
    private WebElementFacade iSiteName;

    @FindBy(name = "uniqueProId")
    private WebElementFacade iUniqueProtocolID;

    @FindBy(name = "secondProId")
    private WebElementFacade tSecondaryIDs;

    @FindBy(name = "prinInvestigator")
    private WebElementFacade iPrincipalInvestigator;

    @FindBy(name = "description")
    private WebElementFacade tBriefSummary;

    @FindBy(name = "protocolDateVerification")
    private WebElementFacade iProtocolDateVerification;

    @FindBy(name = "startDate")
    private WebElementFacade iStartDate;
    
    @FindBy(name = "endDate")
    private WebElementFacade iEstimatedCompletionDate;
    
    @FindBy(name = "expectedTotalEnrollment")
    private WebElementFacade iExpectedTotalEnrollment;
    
    @FindBy(name = "facName")
    private WebElementFacade iFacilityName;
    
    @FindBy(name = "facCity")
    private WebElementFacade iFacilityCity;
    
    @FindBy(name = "facState")
    private WebElementFacade iFacilityState;
    
    @FindBy(name = "facZip")
    private WebElementFacade iFacilityZIP;
    
    @FindBy(name = "facCountry")
    private WebElementFacade iFacilityCountry;
    
    @FindBy(name = "facConName")
    private WebElementFacade iFacilityContactName;
    
    @FindBy(name = "facConDegree")
    private WebElementFacade iFacilityContactDegree;
    
    @FindBy(name = "facConPhone")
    private WebElementFacade iFacilityContactPhone;
    
	@FindBy(name = "facConEmail")
    private WebElementFacade iFacilityContactEmail;
    
    @FindBy(name = "statusName")
    private WebElementFacade iSiteStatusForThisStudy;
    
    @FindBy(id = "createSubStudyForm")
    private WebElementFacade formWithData;
    
    private RadioButtonGroup rInterviewDateDefault;
	
    private RadioButtonGroup rCollectInterviewDate;
	
    private RadioButtonGroup rInterviewerNameDefault;
	
    private RadioButtonGroup rCollectInterviewerName;
    
    private RadioButtonGroup rCollectPersonID;
   
    

    public CreateNewSitePage (WebDriver driver) {
    	super(driver);
    }
    
    public void fillInCreateNewSitePage(Study subStudy) {
    	iSiteName.type(subStudy.getStudyName());

    	iUniqueProtocolID.type(subStudy.getUniqueProtocolID());

        tSecondaryIDs.type(subStudy.getSecondaryIDs());

        iPrincipalInvestigator.type(subStudy.getPrincipalInvestigator());

        tBriefSummary.type(subStudy.getBriefSummary());

        iProtocolDateVerification.type(subStudy.getProtocolDateVerification());

        iStartDate.type(subStudy.getStartDate());
        
        iEstimatedCompletionDate.type(subStudy.getEstimatedCompletionDate());
        
        iExpectedTotalEnrollment.type(subStudy.getExpectedTotalEnrollment());
        
        iFacilityName.type(subStudy.getFacilityName());
        
        iFacilityCity.type(subStudy.getFacilityCity());
        
        iFacilityState.type(subStudy.getFacilityState());
        
        iFacilityZIP.type(subStudy.getFacilityZIP());
        
	    iFacilityCountry.type(subStudy.getFacilityCountry());
        
        iFacilityContactName.type(subStudy.getFacilityContactName());
        
        iFacilityContactDegree.type(subStudy.getFacilityContactDegree());
        
        iFacilityContactPhone.type(subStudy.getFacilityContactPhone());
        
        iFacilityContactEmail.type(subStudy.getFacilityContactEmail());
        
        //iSiteStatusForThisStudy.type(subStudy.getSiteStatusForThisStudy());
        
        defineRadioButtons();
                
    	rInterviewDateDefault.selectByValue(subStudy.getInterviewDateDefault());
        
    	rCollectInterviewDate.selectByValue(subStudy.getCollectInterviewDate());
    	
    	rInterviewerNameDefault.selectByValue(subStudy.getInterviewerNameDefault());
    	
    	rCollectInterviewerName.selectByValue(subStudy.getCollectInterviewerName());
    	
    	rCollectPersonID.selectByValue(subStudy.getInterviewDateDefault());
	}
	
    @Override
	public boolean isOnPage(WebDriver driver) {
    	return (driver.getCurrentUrl().indexOf(PAGE_URL) > -1);
	}
    
    private void defineRadioButtons() {
    	rInterviewDateDefault = new RadioButtonGroup(formWithData.findElements(By.name("interviewDateDefault")));
    	rCollectInterviewDate = new RadioButtonGroup(formWithData.findElements(By.name("interviewDateRequired")));
    	rInterviewerNameDefault = new RadioButtonGroup(formWithData.findElements(By.name("interviewerNameDefault")));
    	rCollectInterviewerName = new RadioButtonGroup(formWithData.findElements(By.name("interviewerNameRequired")));
    	rCollectPersonID = new RadioButtonGroup(formWithData.findElements(By.name("subjectPersonIdRequired")));
    }
}
