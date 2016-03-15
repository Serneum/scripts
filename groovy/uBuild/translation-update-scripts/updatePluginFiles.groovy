import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory
import java.io.*

import javax.xml.transform.*
import javax.xml.transform.dom.*
import javax.xml.transform.stream.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.*

import org.w3c.dom.*

File baseDir = new File(args[0])
int version = 0

DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance()
DocumentBuilder builder = docBuilderFactory.newDocumentBuilder()

XPathFactory xPathFactory = XPathFactory.newInstance()
XPath xpath = xPathFactory.newXPath()

def updateDependenciesXml = {
    def dependenciesFile = new File(baseDir, "dependencies.xml")
    Document doc = builder.parse(dependenciesFile)

    XPathExpression expr = xpath.compile("//dependencies/dependency/cs-project[@name='Groovy']")
    Node csProjectNode = (Node) expr.evaluate(doc, XPathConstants.NODE)
    Node dependencyNode = csProjectNode.parentNode
    Node dependenciesNode = dependencyNode.parentNode

    Element rpxDependencyElement = createRpxDependency(doc)
    dependenciesNode.insertBefore(rpxDependencyElement, dependencyNode.nextSibling)

    writeDocToFile(dependenciesFile, doc)
}

def readPluginVersion = {
    def pluginXmlFile = new File(baseDir, "src/main/zip/plugin.xml")
    Document doc = builder.parse(pluginXmlFile)

    XPathExpression expr = xpath.compile("//plugin/header/identifier")
    Node identifierNode = (Node) expr.evaluate(doc, XPathConstants.NODE)
    Node versionNode = identifierNode.attributes.getNamedItem("version")

    version = Integer.valueOf(versionNode.getTextContent()) + 1
}

def updateInfoXml = {
    def infoXmlFile = new File(baseDir, "src/main/zip/info.xml")
    Document doc = builder.parse(infoXmlFile)

    XPathExpression expr = xpath.compile("(//pluginInfo/release-notes/release-note)[last()]")
    Node lastReleaseNoteNode = (Node) expr.evaluate(doc, XPathConstants.NODE)
    Node releaseNotesNode = lastReleaseNoteNode.parentNode

    Element newReleaseNote = createReleaseNote(doc, version)
    releaseNotesNode.insertBefore(newReleaseNote, lastReleaseNoteNode.nextSibling)

    writeDocToFile(infoXmlFile, doc)
}

def updateUpgradeXml = {
    def upgradeXmlFile = new File(baseDir, "src/main/zip/upgrade.xml")
    Document doc = builder.parse(upgradeXmlFile)

    XPathExpression expr = xpath.compile("(//plugin-upgrade/migrate)[last()]")
    Node lastMigrateNode = (Node) expr.evaluate(doc, XPathConstants.NODE)
    Node pluginUpgradeNode = lastMigrateNode.parentNode

    Element newUpgrade = createUpgrade(doc, version)
    pluginUpgradeNode.insertBefore(newUpgrade, lastMigrateNode.nextSibling)

    writeDocToFile(upgradeXmlFile, doc)
}

def updateGitignore = {
    def gitignore = new File(baseDir, ".gitignore")
    gitignore << "i18n-scraper.groovy\n\n" << "# ignore RPX files\n" << "*.rpxconfig\n" << "*TVT-Map_build*.txt"
}

Element createUpgrade(doc, version) {
    Element upgrade = doc.createElement("migrate")
    upgrade.setAttribute("to-version", String.valueOf(version))
    return upgrade
}

Element createReleaseNote(doc, version) {
    Element releaseNote = doc.createElement("release-note")
    releaseNote.setAttribute("plugin-version", String.valueOf(version))
    releaseNote.textContent = "Added RPX dependency and English translations"
    return releaseNote
}

Element createRpxDependency(doc) {
    Element rpxDependencyElement = doc.createElement("dependency")

    Element csProjectElement = doc.createElement("cs-project")
    csProjectElement.setAttribute("name", "RPX")

    Element criteriaElement = doc.createElement("criteria")
    criteriaElement.setAttribute("status", "Success")

    Element deliveryElement = doc.createElement("delivery")
    deliveryElement.setAttribute("artifact-set", "Default")

    Element directoryElement = doc.createElement("directory")
    directoryElement.setTextContent("lib/build/rpx")

    rpxDependencyElement.appendChild(csProjectElement)
    rpxDependencyElement.appendChild(criteriaElement)
    rpxDependencyElement.appendChild(deliveryElement)
    deliveryElement.appendChild(directoryElement)

    return rpxDependencyElement
}

/**
 * Replace an existing xml file with the contents of an xml document
 */
void writeDocToFile(file, doc) {
    TransformerFactory transformerFactory = TransformerFactory.newInstance()
    Transformer transformer = transformerFactory.newTransformer()
    transformer.setOutputProperty(OutputKeys.INDENT, "yes")
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    // preserve doctype
    DocumentType documentType = doc.getDoctype();
    if (documentType != null) {
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, documentType.getSystemId());
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, documentType.getPublicId());
    }
    StringWriter sw = new StringWriter()
    StreamResult result = new StreamResult(sw)
    DOMSource source = new DOMSource(doc)
    transformer.transform(source,result)
    String xmlString = sw.toString()

    // Delete the file.
    file.delete()
    // Write out our modified DOM to the file with Logger elements stripped.
    file.createNewFile()
    OutputStream outputStream = new FileOutputStream(file,false)
    try {
        outputStream.write( xmlString.getBytes() )
    } finally {
        if (outputStream != null) { outputStream.close() }
    }
}

updateDependenciesXml()
readPluginVersion()
updateInfoXml()
updateUpgradeXml()
updateGitignore()
