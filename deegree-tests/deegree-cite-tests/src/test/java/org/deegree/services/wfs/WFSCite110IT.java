/**
 * 
 */
package org.deegree.services.wfs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.deegree.services.CiteWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Wraps the execution of the CITE WFS 1.1.0 TestSuite as a JUnit-test.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 22950 $, $Date: 2010-03-09 19:05:17 +0100 (Di, 09. Mär
 *          2010) $
 */
@RunWith(Parameterized.class)
public class WFSCite110IT {

	private static String CITE_SCRIPT_PROP = "cite.script";

	private String testLabel = "WFS110";

	private String resultSnippet;

	public WFSCite110IT(String testLabel, String resultSnippet) {
		this.testLabel = testLabel;
		this.resultSnippet = resultSnippet;
	}

	@Parameters
	public static Collection getResultSnippets() throws Exception {

		URL url = WFSCite110IT.class.getResource("/citewfs110/src/main.xml");
		String file = new File(url.toURI()).getAbsolutePath();

		CiteWrapper wrapper = new CiteWrapper(file);
		try {
			wrapper.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String out = wrapper.getOutput();
		String err = wrapper.getError();

		System.out.println(out);
		if (!err.isEmpty()) {
			System.out.println("Standard error messages: " + err);
		}

		return getResultSnippets(out);
	}

	private static Collection getResultSnippets(String out) throws IOException {

		List resultSnippets = new ArrayList();

		BufferedReader reader = new BufferedReader(new StringReader(out));
		List<String> lines = new ArrayList<String>();
		String line = null;
		while ((line = reader.readLine()) != null) {
			lines.add(line);
		}

		int currentLine = 0;
		while (currentLine < lines.size()) {
			String trimmed = lines.get(currentLine++).trim();
			if (trimmed.startsWith("Testing") && !trimmed.startsWith("Testing suite")) {
				String s = trimmed.substring(8);
				String caseId = s.substring(0, s.indexOf(' '));
				String result = findCorrespondingResult(lines, currentLine, caseId);
				resultSnippets.add(new Object[] { caseId, result });
			}
		}
		return resultSnippets;
	}

	private static String findCorrespondingResult(List<String> lines, int currentLine, String caseId) {
		while (currentLine < lines.size()) {
			String trimmed = lines.get(currentLine++).trim();
			if (trimmed.startsWith("Test " + caseId)) {
				return trimmed;
			}
		}
		throw new RuntimeException("Error parsing CITE result log.");
	}

	@Test
	public void singleTest() {
		if (resultSnippet.contains("Failed")) {
			throw new RuntimeException("Test '" + testLabel + "' failed.");
		}
	}
}
