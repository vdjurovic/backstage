Package: ${appSafeName}
<#if publisher?has_content && publisher_email?has_content>
Maintainer: ${publisher} <${publisher_email}>
</#if>
Version: ${version}
Architecture: ${deb_arch}
Description: ${description}
Homepage: ${appUrl}


