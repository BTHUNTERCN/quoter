/**************************************************************************************************************
 * Quoter.jar
 * 
 * Quoter is a command line program that display stock quotes and index data.
 * 
 * *  Copyright (c) 2019-2022 Michael Fross
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *   
 ***************************************************************************************************************/

package org.fross.quoter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fross.library.Debug;
import org.fross.library.Output;
import org.fross.library.URLOperations;
import org.fusesource.jansi.Ansi;

public class Index {
	/**
	 * GetIndex: Returns an array of Strings that contains the Dow, Nasdaq, and S&P data. Unfortunately I have to scrape a
	 * web page for this information as IEX Cloud does not contain index data.
	 * 
	 * @param idx
	 * @return
	 */
	protected static String[] getIndex(String idx) {
		String[] retArray = new String[6];
		String idxPage;
		String URLTEMPLATE = "https://www.marketwatch.com/investing/index/SYMBOLHERE";
		String URL = "ERROR";
		String[] searchPatterns = new String[6];

		// Ensure a valid value was passed
		switch (idx.toUpperCase()) {
		case "DOW":
			URL = URLTEMPLATE.replaceAll("SYMBOLHERE", "djia");
			break;
		case "NASDAQ":
			URL = URLTEMPLATE.replaceAll("SYMBOLHERE", "comp");
			break;
		case "S&P":
			URL = URLTEMPLATE.replaceAll("SYMBOLHERE", "spx");
			break;
		default:
			Output.fatalError("Call to getIndex() must be 'DOW', 'NASDAQ', or 'S&P'", 4);
			break;
		}
		Output.debugPrint("Index URL rewritten to: " + URL);

		try {
			// Download the web page with
			idxPage = URLOperations.ReadURL(URL);

			// Define the regular expression patterns to look for in the URL provided above
			// Current Price
			searchPatterns[1] = "\"price\"\\s+content=\"(.*?)\"";

			// Change
			searchPatterns[2] = "\"priceChange\"\\s+content=\"(.*?)\"";

			// Change Percent
			searchPatterns[3] = "\"priceChangePercent\"\\s+content=\"(.*?)\"";

			// 52Week High
			searchPatterns[4] = "\"label\"\\>52 Week Range\\<\\/small>\\n.+?primary\\s+\"\\>.*?\\s-\\s(.+?)\\<";

			// 52Week Low
			searchPatterns[5] = "\"label\"\\>52 Week Range\\<\\/small>\\n.+?primary\\s+\"\\>(.+?)\\s";

			// Set the first element of the return array to the index name
			retArray[0] = idx;

			// Loop through each search condition and look for matches
			for (int i = 1; i < searchPatterns.length; i++) {
				Pattern pat = Pattern.compile(searchPatterns[i], Pattern.MULTILINE);
				Matcher m = pat.matcher(idxPage);
				if (m.find()) {
					// Remove any commas / percent signs and assign to return array
					retArray[i] = m.group(1).trim().replaceAll("[%,]", "");
				}
			}

			// If we are in debug mode, display the values we are returning
			if (Debug.query() == true) {
				Output.debugPrint("Index Data Returned from Web:");
				for (int i = 0; i < retArray.length; i++) {
					Output.debugPrint("    " + i + ": " + retArray[i]);
				}
			}

		} catch (Exception ex) {
			Output.printColorln(Ansi.Color.RED, "Unable to get Index data for " + idx + "\n" + ex.getMessage());
		}

		return retArray;
	}

}