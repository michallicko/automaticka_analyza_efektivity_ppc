var StatBot = function() {

	var ENTITY_TYPE = {
		"Campaign" : 1,
		"AdGroup" : 2,
		"Keyword" : 3,
		"Ad" : 4
	};

	var STATS_DEPTH = 120;
	var REPORT_SEND_RATE = 10;
	var report = new Array();
	var blockSize = 0;

	this.run = function() {

		var campaignIterator = AdWordsApp.campaigns().withCondition(
				"Status = ENABLED").get();
		var campaign;
		var locked = false;
		var date;
		var current = sortivoLib.cropTime(new Date());
		while (campaignIterator.hasNext()) {
			campaign = campaignIterator.next();
			logger.info("Processing campaign: " + campaign.getName());
			var currentCampaignId = campaign.getId();
			var latestVer = getLatestVersion(currentCampaignId);
			logger.debug(latestVer);
			var startGroup = null;
			if (!latestVer) {
				date = new Date();
				date = sortivoLib.minusDays(date, STATS_DEPTH);
				date = sortivoLib.cropTime(date);
			} else {
				startGroup = latestVer['entityId'];
				locked = latestVer['locked'];
				logger.debug("there is a latest version " + startGroup);
				date = new Date(latestVer['refDate']);
			}
			logger.info("starting with date: " + date);
			if (!locked) {
				while (date < current) {
					logger.debug("Processing date: " + date);
					report.push(getReport(campaign, date));
					try {
						reportStats(campaign, startGroup, date);
						startGroup = null;
					} catch (ex) {
						logger.error(ex);
						if (report.length != null) {
							sendReport();
						}
						unlock(currentCampaignId);
						throw ex;
					}
					date = sortivoLib.incrementByDay(date);
				}
				unlock(currentCampaignId);
				if (report.length != null) {
					sendReport();
				}
			}else{
				logger.info("campaign locked, proceeding another one.");
			}

		}
	}

	this.reportStats = function(campaign, lastGroupId, date) {

		var groupIterator = campaign.adGroups().withCondition(
				"Status = ENABLED").get();
		var group;
		var items = 0;
		var skipedGroupId = null;
		while (lastGroupId
				&& groupIterator.hasNext()
				&& (skipedGroupId = groupIterator.next().getId()) <= lastGroupId) {
			logger.debug("skipping .. " + skipedGroupId);
			// skip previously proceeded groups
		}

		while (groupIterator.hasNext()) {
			group = groupIterator.next();
			items++;
			blockSize++;

			report.push(getReport(group, date));

			var keywordIterator = group.keywords().withCondition(
					"Status = ENABLED").get();
			var keyword;
			while (keywordIterator.hasNext()) {
				keyword = keywordIterator.next();
				report.push(getReport(keyword, date));
			}

			if (blockSize % 50 == 0) {
				logger.debug("Camp report progress: " + items
						/ groupIterator.totalNumEntities() * 100 + " %");
			}

			if (blockSize >= REPORT_SEND_RATE || !checkRuntimeLimit(startDate)) {
				sendReport();
			}

			if (!checkRuntimeLimit(startDate)) {
				throw new Exception("TimeLimitExceededException",
						"Runtime limit exceeded");
			}
		}
	};
	var reportStats = this.reportStats;

	this.getLatestVersion = function(campaignId) {
		var options = {
			"headers" : {
				"ppchit-adwords-token" : PPC_HIT_TOKEN,
			},
			"method" : "get"
		};

		logger.info("fetching:" + serverName + "/app/adwords-report/?entityId="
				+ campaignId);
		var response = UrlFetchApp.fetch(serverName
				+ "/app/adwords-report/?entityId=" + campaignId, options);
		response = Utilities.jsonParse(response.getContentText());
		logger.info("latest ver is:" + response);
		return response;
	};
	var getLatestVersion = this.getLatestVersion;

	var unlock = function(campaignId) {
		var options = {
			"headers" : {
				"ppchit-adwords-token" : PPC_HIT_TOKEN,
			},
			"method" : "post",
			"payload" : {
				"campaign_id" : campaignId
			}
		};
		var response = UrlFetchApp.fetch(serverName
				+ "app/adwords-report/unlock", options);
		logger.info("Campaign id:" + campaignId + " unlocked.");

	}

	var sendReport = function() {
		var options = {
			"headers" : {
				"ppchit-adwords-token" : PPC_HIT_TOKEN,
			},
			"method" : "post",
			"payload" : {
				"report" : JSON.stringify(report)
			}
		};
		var response = UrlFetchApp.fetch(serverName + "app/adwords-report/",
				options);
		blockSize = 0;
		report = new Array();

	};

	var getReport = function(obj, date) {
		var objRep = {};
		var entityCode = ENTITY_TYPE[obj.getEntityType()];

		var d = sortivoLib.getDateStamp(date);
		var statsObj = obj.getStatsFor(d, d);
		objRep = {
			'entityTypeName' : obj.getEntityType(),
			'entityId' : obj.getId(),
			'refDate' : date,// new Date(Date.UTC(date.getFullYear(),
								// date.getMonth(), date.getDate(), 0, 0,
								// 0)).getTime(),
			'avgCpc' : statsObj.getAverageCpc(),
			'avgCpm' : statsObj.getAverageCpm(),
			'avgPageViews' : statsObj.getAveragePageviews(),
			'avgPosition' : statsObj.getAveragePosition(),
			'avgTimeOnSite' : statsObj.getAverageTimeOnSite(),
			'bounceRate' : statsObj.getBounceRate(),
			'clicks' : statsObj.getClicks(),
			'conversionRate' : statsObj.getConversionRate(),
			'conversions' : statsObj.getConversions(),
			'cost' : statsObj.getCost(),
			'ctr' : statsObj.getCtr(),
			'impressions' : statsObj.getImpressions()
		};

		switch (entityCode) {
		case ENTITY_TYPE.Campaign:
			objRep['budget'] = obj.getBudget();
			objRep['name'] = obj.getName();
			objRep['campaignId'] = obj.getId();
			objRep['adGroupId'] = null;
			break;
		case ENTITY_TYPE.AdGroup:
			objRep['maxCpc'] = obj.getKeywordMaxCpc();
			objRep['name'] = obj.getName();
			objRep['campaignId'] = obj.getCampaign().getId();
			objRep['adGroupId'] = obj.getId();
			break;
		case ENTITY_TYPE.Keyword:
			objRep['maxCpc'] = obj.getMaxCpc();
			objRep['firstPageCpc'] = obj.getFirstPageCpc();
			objRep['topOfPageCpc'] = obj.getTopOfPageCpc();
			objRep['name'] = obj.getText();
			objRep['campaignId'] = obj.getCampaign().getId();
			objRep['adGroupId'] = obj.getAdGroup().getId();
			break;
		case ENTITY_TYPE.Ad:
			objRep['name'] = obj.getHeadline;
			objRep['campaignId'] = obj.getCampaign().getId();
			objRep['adGroupId'] = obj.getAdGroup().getId();
			break;
		}
		return objRep;
	};
};
