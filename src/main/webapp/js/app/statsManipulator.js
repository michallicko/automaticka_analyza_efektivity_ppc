var StatsManipulator = function(){
	
	this.openCampaign = function(campaignId){
		var campBlock = $("#campBlock" + campaignId);
		
		if (campBlock.find(".content").hasClass("loaded")){
			campBlock.find(".content").show();
		}else{
			getCampaignStats(campaignId);
		}
	};
   var openCampaign = this.openCampaign;
	
	this.getCampaignStats = function(campaignId){
		console.log("fetching stats data form campaign id: " + campaignId);
		 $.ajax({
             type: "GET",
             url: rootContext + "app/stats/get_campaign_data?id=" + campaignId
         }).done(function(data){
        	 console.log("data fetched, opening campaign block: " + campaignId);
        	 $("#campBlock" + campaignId).find(".content").addClass("loaded");
        	 //openCampaign(campaignId);
         });        
	};
	var getCampaignStats = this.getCampaignStats;
	
	
   this.getUrlWithDateRange = function(){
		var url = document.URL;
		return setUrlDateRange(url);
   };
   
   var setUrlDateRange = function(url){
	    var data = $(".rangeSelect").serialize();
		var hasParameters = url.indexOf("?");
		if (hasParameters != -1){
			var parameters = url.substring(hasParameters + 1);
			url = url.replace(parameters, data);
			console.log(parameters);
		}else{
			url = url + "?" + data;
		}
		return url;
   };
   this.setUrlDateRange = setUrlDateRange;
	
}