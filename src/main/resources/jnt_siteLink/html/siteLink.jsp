<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="uiComponents" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<c:set var="targetNode" value="${renderContext.site}"/>
<c:if test="${not empty currentNode.properties['targetPageName']}">
    <jcr:node var="targetNode" path="${targetNode.path}/${currentNode.properties['targetPageName'].string}" />
</c:if>

${fn:escapeXml(currentNode.properties['mode'].string)}
<c:url var="targetUrl" value="${url.base}${targetNode.path}.${currentNode.properties['targetTemplate'].string}.html"/>
<div class="${fn:escapeXml(currentNode.properties['divClass'].string)}"><!--start preferences-->
    <a title="${fn:escapeXml(currentNode.displayableName)}" class="${fn:escapeXml(currentNode.properties['aClass'].string)}" href="${fn:escapeXml(targetUrl)}">${fn:escapeXml(currentNode.displayableName)}</a>
</div>