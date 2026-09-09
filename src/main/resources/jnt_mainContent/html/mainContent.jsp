<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<jcr:nodeProperty node="${currentNode}" name="image" var="image"/>

<div class="maincontent">
    <h3 class="title"><jcr:nodeProperty node="${currentNode}" name="jcr:title" var="mainContentTitle"/><c:out value="${mainContentTitle.string}"/></h3>
        <c:if test="${!empty image}">
            <div class="imagefloat${fn:escapeXml(currentNode.properties.align.string)}">
            			<img src="${image.node.url}" alt="${fn:escapeXml(image.node.url)}"/>
                        </div>
        </c:if>
		 ${currentNode.properties.body.string}
</div>
<br class="clear"/>