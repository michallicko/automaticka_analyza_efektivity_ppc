$(function(){
	
	var statsManipulator = new StatsManipulator();
	
	$(".datePicker").datepicker({ dateFormat: 'dd.mm.yy' });
	
	
	
	$(".rangeSelect").submit(function(event){
		event.preventDefault();
		var url = statsManipulator.getUrlWithDateRange();
		
		console.log(url);
		window.location.href = url;
	});
	
	$(document).on("click", ".navig", function(event){
		event.preventDefault();
		var url = statsManipulator.setUrlDateRange($(this).attr("href"));
		window.location.href = url;
	});
	
	$(".tab-controls a").click(function(event){
		event.preventDefault();
		var tabName =  $(this).attr("href");
		if($(this).hasClass("redir")){
			window.location.href = tabName;
		}
		
		if($(this).hasClass("fetch")){
			fetchExternalResource(tabName);
		}
		$(".tab-controls a").removeClass("selected");
		$(this).addClass("selected");
		$(".tab").hide();
		$(".tab-name-"+tabName).show();
		
	});
	
});

function fetchExternalResource(resourceName){
	
	
	
	
}

