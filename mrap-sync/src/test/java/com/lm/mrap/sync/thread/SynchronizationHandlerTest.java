package com.lm.mrap.sync.thread;

import com.lm.mrap.sync.factory.DOMBuilder;
import com.lm.mrap.sync.utils.DOMNodeKeyValue;
import com.lm.mrap.sync.utils.DOMTreeModel;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liming
 * @version 1.0
 * @description: TODO
 * @date 2022/11/18 上午10:59
 */
public class SynchronizationHandlerTest {


    @Test
    public void testParseXMLConfig() throws IOException, ParserConfigurationException, SAXException {
        DOMBuilder domBuilder = new DOMBuilder();

        String resourcePath = "/home/liming/workspace/project/mrap/mrap-sync/src/test/resources/conf/spark_config.xml";

        Document document = domBuilder
                .getDocumentBuilder()
                .parse(
                        Files.newInputStream(Paths.get(resourcePath))
                );

        ConcurrentHashMap<DOMNodeKeyValue, String> xmlConfig = new DOMTreeModel(document).foreachForDOM();


        Assert.assertNotNull(xmlConfig);
    }
}
