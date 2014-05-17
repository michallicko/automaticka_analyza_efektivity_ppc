package cz.sortivo.reporting.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import cz.sortivo.ppcgenerator.exception.LoginRequiredException;
import cz.sortivo.ppcgenerator.model.Account;
import cz.sortivo.ppcgenerator.model.Provider;
import cz.sortivo.ppcgenerator.model.User;
import cz.sortivo.ppcgenerator.service.AdwordsAccountService;
import cz.sortivo.ppcgenerator.service.AuthenticationService;
import cz.sortivo.reporting.model.ChartVisualisation;
import cz.sortivo.reporting.model.EntityType;
import cz.sortivo.reporting.model.Stat;
import cz.sortivo.reporting.model.StatNavigation;
import cz.sortivo.reporting.service.ReportingAccountService;
import cz.sortivo.reporting.service.StatService;

@Controller
@RequestMapping("/stats")
public class ReportingController {

    private static final Logger logger = LoggerFactory.getLogger(ReportingController.class);

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AdwordsAccountService accountService;

    @Autowired
    private StatService reportingService;

    @Autowired
    private ReportingAccountService reportingAccountService;

    private static final String GOOD_CHART_LINE_COLOR = "#FDC400";
    private static final String BAD_CHART_LINE_COLOR = "";

    private DateTime[] getRange(String fromDateString, String toDateString) {
        DateTime[] range = new DateTime[2];
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy");

        if (!StringUtils.isEmpty(fromDateString)) {
            range[0] = formatter.parseDateTime(fromDateString);
        } else {
            range[0] = new DateTime().minusMonths(1).minusDays(1);
        }
        if (!StringUtils.isEmpty(toDateString)) {
            range[1] = formatter.parseDateTime(toDateString);
        } else {
            range[1] = new DateTime().minusDays(1);
        }
        return range;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{provider}", produces = "application/html;charset=UTF-8")
    public ModelAndView getStats(@RequestParam(value = "fromDate", defaultValue = "") String fromDateString,
            @RequestParam(value = "toDate", defaultValue = "") String toDateString,
            @PathVariable("provider") String provider) {

        return getStats(getRange(fromDateString, toDateString), Provider.getInstanceByName(provider), null, null, null,
                EntityType.CAMPAIGN);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{provider}/{accountId}", produces = "application/html;charset=UTF-8")
    public ModelAndView getStatsForAccount(@RequestParam(value = "fromDate", defaultValue = "") String fromDateString,
            @RequestParam(value = "toDate", defaultValue = "") String toDateString,
            @PathVariable("accountId") Long accountId, @PathVariable("provider") String provider) {

        return getStats(getRange(fromDateString, toDateString), Provider.getInstanceByName(provider), accountId, null,
                null, EntityType.CAMPAIGN);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{provider}/{accountId}/{entity1}", produces = "application/html;charset=UTF-8")
    public ModelAndView getStatsFirstLevel(@RequestParam(value = "fromDate", defaultValue = "") String fromDateString,
            @RequestParam(value = "toDate", defaultValue = "") String toDateString,
            @PathVariable("accountId") Long accountId, @PathVariable("provider") String provider,
            @PathVariable("entity1") Long campaignId) {
        return getStats(getRange(fromDateString, toDateString), Provider.getInstanceByName(provider), accountId,
                campaignId, null, EntityType.ADGROUP);

    }

    @RequestMapping(method = RequestMethod.GET, value = "/{provider}/{accountId}/{entity1}/{entity2}/{subentities}", produces = "application/html;charset=UTF-8")
    public ModelAndView getStatsSecondLevel(@RequestParam(value = "fromDate", defaultValue = "") String fromDateString,
            @RequestParam(value = "toDate", defaultValue = "") String toDateString,
            @PathVariable("provider") String provider, @PathVariable("accountId") Long accountId,
            @PathVariable("entity1") Long campaignId, @PathVariable("entity2") Long groupId,
            @PathVariable("subentities") String subentities) {

        DateTime range[] = getRange(fromDateString, toDateString);
        ModelAndView mav = getStats(range, Provider.getInstanceByName(provider), accountId, campaignId, groupId,
                EntityType.getInstance(subentities));
        mav.addObject("subEntityName", subentities.toLowerCase());
        mav.addObject("worstKws", null);
        return mav;
    }

    private ChartVisualisation generateKwsChartData(List<Object[]> kws, String title, String labels, String id,
            String rowOptions) {

        // String labels =
        // "['Klíčové slovo', 'Cena za konverzi', { role: 'style' }],";
        String data = "";
        Iterator<Object[]> kwsStatIterator = kws.iterator();

        Object[] stats;
        while (kwsStatIterator.hasNext()) {
            stats = kwsStatIterator.next();
            data += "[";
            for (int i = 0; i < stats.length; i++) {
                if(stats[i] instanceof String){
                    data += "'" + stats[i] + "'";
                }else{
                    data += stats[i];
                }
                
                
                if (i != stats.length - 1) {
                    data += ", ";
                }
            }
            data += ",'" + rowOptions + "']";

            if (kwsStatIterator.hasNext()) {
                data += ",";
            }
        }
        return new ChartVisualisation(title, labels + data, id);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{provider}/{accountId}/{entity1}/{entity2}", produces = "application/html;charset=UTF-8")
    public String getStatsSecondLevelDefault(
            @RequestParam(value = "fromDate", defaultValue = "") String fromDateString,
            @RequestParam(value = "toDate", defaultValue = "") String toDateString,
            @PathVariable("provider") String provider, @PathVariable("accountId") Long accountId,
            @PathVariable("entity1") Long campaignId, @PathVariable("entity2") Long groupId, HttpServletRequest request) {
        return "redirect:" + request.getRequestURI() + "/keyword";

    }

    private List<StatNavigation> makeNavigation(Provider provider, Account account, Long campaignId, Long groupId,
            EntityType subTypes) {

        List<StatNavigation> navigElements = new LinkedList<>();

        String campaignName = reportingService.getNameForEntityId(campaignId, EntityType.CAMPAIGN);
        String groupName = reportingService.getNameForEntityId(groupId, EntityType.ADGROUP);
        String currentAction = "";
        if (provider != null) {
            currentAction += "/" + provider.toString().toLowerCase();
            navigElements.add(new StatNavigation(provider.name(), currentAction));
        } else {
            return navigElements;
        }

        if (account != null) {
            currentAction += "/" + account.getAccountId();
            navigElements.add(new StatNavigation(account.getUserName(), currentAction, "Účet"));
        } else {
            return navigElements;
        }

        if (campaignName != null) {
            currentAction += "/" + campaignId;
            navigElements.add(new StatNavigation(campaignName, currentAction, "Kampaň"));
        } else {
            return navigElements;
        }

        if (groupName != null) {
            currentAction += "/" + groupId;
            navigElements.add(new StatNavigation(groupName, currentAction, "Skupina"));
        } else {
            return navigElements;
        }

        // if(subTypes != null){
        // currentAction += "/"+subTypes.toString().toLowerCase();
        // navigElements.add(new
        // StatNavigation(subTypes.toString().toLowerCase(), currentAction ));
        // }

        return navigElements;
    }

    public ModelAndView getStats(DateTime[] range, Provider provider, Long accountId, Long campaignId, Long groupId,
            EntityType subType) {

        ModelAndView mav = new ModelAndView("stats/index");

        User user = authenticationService.getUser();

        if (user == null) {
            throw new LoginRequiredException();
        }

        DateTime from = range[0];
        DateTime to = range[1];

        Collection<Account> accounts = null;
        Account currentAccount = null;
        if (accountId == null) {
            accounts = reportingAccountService.getAccounts(user, provider);
        } else {
            accounts = new LinkedList<>();
            currentAccount = reportingAccountService.getUserAccountByIdAndProvider(accountId, user, provider);
            accounts.add(currentAccount);
        }

        List<Stat> entityStats = reportingService.getStats(from, to, campaignId, accounts, groupId, subType, provider,
                user);

        List<StatNavigation> navigation = makeNavigation(provider, currentAccount, campaignId, groupId, subType);

        StatNavigation superElement = null;
        if (navigation != null && navigation.size() > 1) {
            superElement = navigation.get(navigation.size() - 1);
        }

        Map<String, Object> balanceTextParams = reportingService.getBalanceTextParameters(groupId, from, to,
                campaignId, provider);
        List<Object[]> bestKwsPricePerConv = reportingService.getBestKeywords(range[0], range[1], campaignId, groupId,
                provider, 10);
        List<Object[]> bestKwsConvCount = reportingService.getBestKeywordsByConvCount(range[0], range[1], campaignId,
                groupId, provider, 10);
        List<Object[]> bestKwsConvScore = reportingService.getBestKeywordsByConvScore(range[0], range[1], campaignId,
                groupId, provider, 10);
        List<Object[]> bestKwsClickScore = reportingService.getBestKeywordsByClickScore(range[0], range[1], campaignId,
                groupId, provider, 10);
        List<Object[]> worstKwsClickScore = reportingService.getWorstKeywordsByClickScore(range[0], range[1],
                campaignId, groupId, provider, 10);
        List<Object[]> worstKwsConvScore = reportingService.getWorstKeywordsByConvScore(range[0], range[1],
                campaignId, groupId, provider, 10);

        List<ChartVisualisation> chartsData = new LinkedList<>();

        if (bestKwsClickScore != null) {
            chartsData.add(generateKwsChartData(bestKwsClickScore, "Nejlepší klíčová slova",
                    "['Klíčové slovo', 'Počet prokliků','Cena', { role: 'style' }],", "bestKwsClickScore",
                    GOOD_CHART_LINE_COLOR));
        }
        
        if (bestKwsClickScore != null) {
            chartsData.add(generateKwsChartData(worstKwsClickScore, "Nejhorší klíčová slova",
                    "['Klíčové slovo', 'Počet prokliků','Cena', { role: 'style' }],", "worstKwsClickScore",
                    GOOD_CHART_LINE_COLOR));
        }
        
        if (bestKwsClickScore != null) {
            chartsData.add(generateKwsChartData(bestKwsConvScore, "Nejlepší klíčová slova",
                    "['Klíčové slovo', 'Počet konverzí','Cena za konverzi', { role: 'style' }],", "bestKwsConvScore",
                    GOOD_CHART_LINE_COLOR));
        }
        
        if (bestKwsClickScore != null) {
            chartsData.add(generateKwsChartData(worstKwsConvScore, "Nejhorší klíčová slova",
                    "['Klíčové slovo', 'Počet konverzí','Cena za konverzi', { role: 'style' }],", "worstKwsConvScore",
                    GOOD_CHART_LINE_COLOR));
        }

        if (bestKwsPricePerConv != null && bestKwsPricePerConv.size() > 0) {
            balanceTextParams.put("bestKW_ppconv", bestKwsPricePerConv.get(0));
        }
        if (bestKwsConvCount != null && bestKwsConvCount.size() > 0) {
            balanceTextParams.put("bestKW_convcnt", bestKwsConvCount.get(0));
        }

        DateTimeFormatter fmt = DateTimeFormat.forPattern("dd.MM.yyy");
        mav.addObject("availableEntities", entityStats);
        mav.addObject("dateTo", to.toString(fmt));
        mav.addObject("provider", provider.name().toLowerCase());
        mav.addObject("accounts", accounts);
        mav.addObject("dateFrom", from.toString(fmt));
        mav.addObject("chartsData", chartsData);
        mav.addObject("balanceTextParams", balanceTextParams);
        mav.addObject("level", subType);
        mav.addObject("superElement", superElement);
        mav.addObject("navigation", navigation);
        return mav;
    }
}
