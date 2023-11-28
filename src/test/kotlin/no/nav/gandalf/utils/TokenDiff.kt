package no.nav.gandalf.utils

import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.IOException
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

@Throws(ParserConfigurationException::class, SAXException::class, IOException::class)
internal fun diffTokens(token1: String, token2: String): List<String>? {
    // get document elements
    val dbFact = DocumentBuilderFactory.newInstance()
    val docBuilder = dbFact.newDocumentBuilder()
    val doc1 = docBuilder.parse(InputSource(StringReader(token1)))
    val doc2 = docBuilder.parse(InputSource(StringReader(token2)))
    doc1.documentElement.normalize()
    doc2.documentElement.normalize()
    val diff: MutableList<String> = ArrayList()

    // check top node = assertion node
    if (doc1.childNodes.length != 1 || !doc1.firstChild.nodeName.contains("Assertion")) {
        diff.add("token1 har ikke assertion node på top, eller har flere noder (ant nodes: " + doc1.childNodes.length + " node name firstchild: " + doc1.firstChild.nodeName)
    }
    if (doc2.childNodes.length != 1 || !doc2.firstChild.nodeName.contains("Assertion")) {
        diff.add("token1 har ikke assertion node på top, eller har flere noder (ant nodes: " + doc2.childNodes.length + " node name firstchild: " + doc2.firstChild.nodeName)
    }

    // diff top node
    diffNode(doc1.firstChild, doc2.firstChild, diff)
    return diff
}

private fun diffNode(n1: Node, n2: Node, diff: MutableList<String>): List<String>? {
    if (getNodeName(n1) != getNodeName(n2)) {
        diff.add("Node name differs, n1=" + getNodeName(n1) + " and n2=" + getNodeName(n2))
        return diff
    }
    // Skip comparing these nodes
    if (n1.nodeName == "Signature") {
        return diff
    }
    if (n1.nodeName != "saml2:Assertion" &&
        n1.nodeName != "saml2:AttributeValue" &&
        n1.nodeName != "saml2:AttributeStatement" &&
        (
            n1.textContent == null && n2.textContent != null ||
                n1.textContent != null && n1.textContent != n2.textContent
            )
    ) {
        diff.add("Node " + getNodeName(n1) + " token1 has textcontent " + n1.textContent + " token2 has " + n2.textContent)
    }
    diffAttributes(n1, n2, diff)
    if (n1.nodeName != "saml2:AttributeValue") {
        diffChildren(n1, n2, diff)
    }
    return diff
}

private fun diffAttributes(n1: Node, n2: Node, diff: MutableList<String>): List<String>? {
    val map1 = n1.attributes
    val map2 = n2.attributes
    if (map1 == null && map2 == null) {
        return diff
    }
    if (map1 == null && map2 != null || map1 != null && map2 == null) {
        diff.add("Node " + getNodeName(n1) + " has different attributes")
        return diff
    }
    var j = 0
    for (i in 0 until map1!!.length) {
        if (j < map2!!.length && map1.item(i).nodeName == map2.item(j).nodeName) {
            if (map1.item(i).textContent == null && map2.item(j).textContent != null ||
                map1.item(i).textContent != null && map1.item(i).textContent != map2.item(j).textContent
            ) {
                diff.add("Node " + getNodeName(n1) + " Attribute " + map1.item(i).nodeName + " has different content: token1 has " + map1.item(i).textContent + " token2 has " + map2.item(j).textContent)
            }
            j++
        } else {
            diff.add("token1 node " + getNodeName(n1) + " has attribute " + map1.item(i).nodeName + ", missing in token2")
        }
    }
    // bør samle lister av de til overs og sammenligne disse TODO
    while (j < map2!!.length) {
        diff.add("token2 has node " + getNodeName(n2) + " has attribute " + map2.item(j).nodeName + ", missing in token2")
        j++
    }
    return diff
}

private fun diffChildren(n1: Node, n2: Node, diff: MutableList<String>): List<String>? {
    val nList1 = n1.childNodes
    val nList2 = n2.childNodes
    var j = 0
    for (i in 0 until nList1.length) {
        if (j < nList2.length && getNodeName(nList1.item(i)) == getNodeName(nList2.item(j))) {
            diffNode(nList1.item(i), nList2.item(j), diff)
            j++
        } else if (!isMemberOf(nList1.item(i), nList1)) { // er duplikate attributter i saml token fra datapower
            diff.add("token1 node " + n1.nodeName + " has child " + getNodeName(nList1.item(i)) + ", missing in token2")
        }
    }
    while (j < nList2.length) {
        diff.add("token2 node " + n2.nodeName + " has child " + getNodeName(nList2.item(j)) + ", missing in token1")
        j++
    }
    return diff
}

private fun isMemberOf(n: Node, nList: NodeList): Boolean {
    for (i in 0 until nList.length) {
        if (getNodeName(nList.item(i)) == getNodeName(n)) {
            return true
        }
    }
    return false
}

private fun getNodeName(n: Node): String? {
    return if (n.nodeName == "saml2:Attribute") {
        "saml2:Attribute:" + n.attributes.getNamedItem("Name").nodeValue
    } else n.nodeName
}
