<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
 <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
 <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<t:otherpage>
	<jsp:attribute name="title">
		Statistiky exportu
	</jsp:attribute>

	<jsp:attribute name="javascript">
    <c:url value="/" var="rootContext"/>
    <script>var rootContext = "${rootContext}"</script>
    <link rel="stylesheet" href="<c:url value="/css/app/stats.css" />" type="text/css" media="all" />
    <script type="text/javascript" src="https://www.google.com/jsapi"></script>
    <script src="<c:url value="/js/app/statsManipulator.js?v=140332" />" type="text/javascript"></script>
    <script src="<c:url value="/js/app/statsActions.js?v=140332" />" type="text/javascript"></script>
    
	</jsp:attribute>
    <jsp:body>
    <c:if test="${not empty superElement}">
        <h2>${superElement.levelName}: ${superElement.elementName}</h2>
    </c:if>
    <h3>${provider } statistiky za období ${dateFrom } - ${dateTo }</h3>
         <form class="rangeSelect form-inline" action="setStatRange">
            <input class="datePicker" type="text" placeholder="od" name="fromDate" value="${dateFrom }"/> - 
            <input class="datePicker" type="text" placeholder="do" name="toDate" value="${dateTo }"/>
            <button class="btn btn-default"  type="submit" >Zvolit</button> 
        </form>
    <div class="breadcrumbs">
    <c:forEach var="n" items="${ navigation }">
        <span class=navigElement">&gt; <a class="navig" href="<c:url value="/app/stats${n.actionName}"/>">${n.elementName}</a></span> 
    </c:forEach>
    </div>
    <div class="tab-controls">
    <c:choose>
        <c:when test="${superElement.levelName == 'Skupina' }">
            <a href="<c:url value="/app/stats${navigation[fn:length(navigation)-1].actionName}/keyword"/>" class="redir <c:if test="${subEntityName == 'keyword'}">selected</c:if>">Klíčová slova</a>
            <a href="<c:url value="/app/stats${navigation[fn:length(navigation)-1].actionName}/ad"/>" class="redir <c:if test="${subEntityName == 'ad'}">selected</c:if>">Reklamy</a>
        </c:when>
        <c:otherwise>
            <a href="balance" class="fetch">Bilance</a>
            <a href="statlisting" class="selected">Statistiky</a>
        </c:otherwise>
    </c:choose>
       
        <div class="cleaner"></div>
    </div>
    <div class="tab tab-name-statlisting tab-selected">
        <table class="table table-striped">
            <thead>
                <tr><th>Název</th><th>Prokliky</th><th>Cena za proklik</th><th>Míra Proklikovosti</th><th>Zobrazení</th><th>Počet konverzí</th><th>Míra konverze</th><th>Průměrná pozice</th><th>Celková cena</th></tr>
            </thead>
            <tbody>
                <c:forEach var="c" items="${ availableEntities }">
                    <tr>
                        <th><a class="navig" href="<c:url value="/app/stats${navigation[fn:length(navigation)-1].actionName}/${c.entityId }"/>">${c.name}</a></th>
                        <td>${c.clicks}</td>
                        <td><fmt:formatNumber value="${c.avgCpc/100}" maxFractionDigits="2"/> Kč</td>
                        <td><fmt:formatNumber value="${c.ctr*100}" maxFractionDigits="2"/> %</td>
                        <td>${c.impressions}</td>
                        
                        <td>${c.conversions}</td>
                        <td><fmt:formatNumber value="${c.conversionRate*100}" maxFractionDigits="2"/> %</td>
                        <td><fmt:formatNumber value="${c.avgPosition}" maxFractionDigits="2"/></td>
                        <td>${c.cost/100} Kč</td>
                    </tr>
                </c:forEach>
             </tbody>
        </table>
    </div>
            <div class="tab tab-name-balance">
                <script type="text/javascript">
                  google.load("visualization", "1", {packages:["corechart"]});
                  google.setOnLoadCallback(drawChart);
                  function drawChart() {
                	  var rawData = {};
                              
                  <c:forEach items="${chartsData}" var="data">
                          rawData["${data.id}"] = {'data': [${data.data}], 'title': "${data.title}"};     
                  </c:forEach> 
        
                   for(var key in rawData){
                	   var data = google.visualization.arrayToDataTable(rawData[key]['data']);
                	   var options = {
                               'title': rawData[key]['title'],
                               chartArea: { left: "50%", width: "35%", height: "70%" },
                               'legend': {position: 'none'},
                               'width': 520,
                               'height': 300
                             };
                	   var chart = new google.visualization.BarChart(document.getElementById(key));
                       chart.draw(data, options);
                   }    
  
                  }
                  </script>
                  <style>.kwChart{width: 520px; height: 300px; float:left;} .cleaner{clear: both;}</style>
                  <h2>Bilance pro ${superElement.levelName} <span class="bold">${superElement.elementName}</span></h2>
                  <p>${superElement.levelName} <span class="bold">${superElement.elementName}</span> obsahuje <span class="bold">${balanceTextParams['total_keywords'] }</span> aktivních klíčových slov, z toho <span class="bold">${balanceTextParams['clicked_keywords'] }</span> slov vykazuje prokliky a <span class="bold">${balanceTextParams['converted_keywords'] }</span> je konverzních.</p>
      
                  <h3>Proklikovost</h3>
                    <c:choose>
                    <c:when test="${balanceTextParams['clicked_keywords'] != '0'}">
                  <div class="kwChart balanceChart" id="bestKwsClickScore"></div>
                  <div class="kwChart balanceChart" id="worstKwsClickScore"></div>
                  <div class="cleaner"></div>
                  
                    </c:when>
                    <c:otherwise>
                        <div class="alert">Na této úrovni nevykazují žádná klíčová slova prokliky.</div>
                    </c:otherwise>
              
                  </c:choose>
                    <div class="alert alert-info">
                        <strong>Vaše klíčová slova nedosahují maximálního výkonu</strong><br>
                        <span class="bold">TIP:</span> zvažte zvýšení ceny za proklik zejména u klíčových slov <span class="bold">plastové sudy na deštovou vodu</span>, <span class="bold">"plastový sud s víkem"</span>, která disponují vysokým počtem zobrazení, ale zobrazují se nejníže.
                        
                              
                    </div>
                  <h3>Konverze</h3>
                  <c:choose>
                    <c:when test="${balanceTextParams['converted_keywords'] != 0}">
                     <div class="kwChart balanceChart" id="bestKwsConvScore"></div>
                  <div class="kwChart balanceChart" id="worstKwsConvScore"></div>
                  <div class="cleaner"></div>
                  <p><span class="bold">${balanceTextParams['bestKW_convcnt'][0]}</span> je klíčové slovo s nejvyšším počtem (<span class="bold">${balanceTextParams['bestKW_convcnt'][3]}</span>) konverzí za které bylo utraceno celkem <span class="bold">${balanceTextParams['bestKW_convcnt'][2]} Kč</span>, pruměrně <span class="bold">${balanceTextParams['bestKW_convcnt'][1]} Kč</span> za konverzi. Nejlevnějších konverzí dosahuje klíčové slovo 
                  <span class="bold">${balanceTextParams['bestKW_ppconv'][0]}</span> v průměru za <span class="bold">${balanceTextParams['bestKW_ppconv'][1]}</span> Kč. Těchto konverzí bylo uskutečněno <span class="bold">${balanceTextParams['bestKW_ppconv'][2]}</span>. Průměrná pozice konverzních slov je <span class="bold"><fmt:formatNumber value="${balanceTextParams['converted_keywords_avg_pos']}" maxFractionDigits="2"/></span>.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="alert">Na této úrovni nemáte žádná konverzní klíčová slova.</div>
                    </c:otherwise>
                  </c:choose>
                  <p>Nekonverzní klíčová slova Vás za danné období stála <span class="bold"><fmt:formatNumber value="${balanceTextParams['nonconverted_keywords_total_cost']}" maxFractionDigits="2"/></span> Kč a jejich počet je <span class="bold">${balanceTextParams['nonconverted_keywords_total_count'] }</span>.</p>  
                  <div class="alert alert-info">
                  <strong>Nízký konverzní poměr</strong><br>
                  Klíčová slova jako <span class="bold">[dílenské skříně]</span> dosahují vysoké proklikovosti, ale nízkého konverzního poměru.
                  <span class="bold">TIP:</span>Analyzujte strukturu Vašich stránek. 
                  <ul>
                    <li>Lze naplnit konverzi v co nejmenším počtu kroků?</li>
                    <li>Je na vstupní stránce správně umístěn ovládací prvek umožňující naplnění konverze?</li>
                  </ul> 
                  </div>
            </div>
    </jsp:body>
</t:otherpage>