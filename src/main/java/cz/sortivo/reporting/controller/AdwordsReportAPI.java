package cz.sortivo.reporting.controller;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cz.sortivo.ppcgenerator.exception.LoginRequiredException;
import cz.sortivo.ppcgenerator.model.AdwordsAccount;
import cz.sortivo.ppcgenerator.service.AdwordsAccountService;
import cz.sortivo.reporting.model.AdWordsReportState;
import cz.sortivo.reporting.model.StatFormatAdwordsScriptsImpl;
import cz.sortivo.reporting.model.StatFormatAdwordsScriptsSingleReport;
import cz.sortivo.reporting.service.AdwordsScriptsStatService;
import cz.sortivo.reporting.service.StatService;

@Controller
@RequestMapping("/adwords-report")
public class AdwordsReportAPI {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportingController.class);
    
    @Autowired
    private AdwordsAccountService accountService;
    
    @Autowired
    private StatService reportingService;
    
    @Autowired
    private AdwordsScriptsStatService statCreator;
    
    
    /**
     * Method for submitting stat reports in required format specified below
     * @param headerToken - ppcHit authorization token used for mapping report to an existing account
     * @param reportJSON - json representation of statistics received from an adwords account
     */
    @SuppressWarnings({"unchecked" })
    @RequestMapping(value="/", method = RequestMethod.POST)
    @ResponseBody
    public void adWordsStats(@RequestHeader("ppchit-adwords-token") String headerToken,
            @RequestParam("report") String reportJSON ) {
        
        ObjectMapper jsonMapper = new ObjectMapper();
       
        //require token authorization
        AdwordsAccount account = accountService.getAccountByToken(headerToken);
        if (account == null){
            logger.error("Account with specified token doesn't exist");
            throw new LoginRequiredException();
        }
        
        try {
            //parse string to Map object
            List<StatFormatAdwordsScriptsSingleReport> statFormat = jsonMapper.readValue(reportJSON , new TypeReference<List<StatFormatAdwordsScriptsSingleReport>>() {});

            reportingService.saveStats(statCreator, new StatFormatAdwordsScriptsImpl(statFormat), account);
                        
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("User: " + account.getUser().getEmail() + " reported stats.");
        
        
    }
    
    /**
     * Method for submitting stat reports in required format specified below
     * @param headerToken - ppcHit authorization token used for mapping report to an existing account
     * @param reportJSON - json representation of statistics received from an adwords account
     */
    @SuppressWarnings({"unchecked" })
    @RequestMapping(value="/unlock", method = RequestMethod.POST)
    @ResponseBody
    public void unlock(@RequestHeader("ppchit-adwords-token") String headerToken,
            @RequestParam("campaign_id") Long campaignId ) {
        
        //require token authorization
        AdwordsAccount account = accountService.getAccountByToken(headerToken);
        if (account == null){
            logger.error("Account with specified token doesn't exist");
            throw new LoginRequiredException();
        }
        
        reportingService.unlock(campaignId, account.getUser());
       
        logger.info("User: " + account.getUser().getEmail() + " unlocked campaign id: " + campaignId);
    }
    
    @RequestMapping(value="/", method = RequestMethod.GET)
    @ResponseBody
    public AdWordsReportState adWordsStats(@RequestHeader("ppchit-adwords-token") String headerToken,
            @RequestParam("entityId") Long entityId) {
        
        //require token authorization
        AdwordsAccount account = accountService.getAccountByToken(headerToken);
        if (account == null){
            logger.error("Account with specified token doesn't exist");
            throw new LoginRequiredException();
        }
        logger.info("User: " + account.getUser() + " requesting info for entity id: " + entityId);
       
        AdWordsReportState adwordsReportState = reportingService.getLatestVersion(account, entityId);
        
        if (adwordsReportState != null){
            logger.info("Lastest is: " + adwordsReportState.getRefDate());    
        }else{
            logger.info("New entity, no items for specified id were found!");
        }
        return adwordsReportState;
    }
    
    
    
}
