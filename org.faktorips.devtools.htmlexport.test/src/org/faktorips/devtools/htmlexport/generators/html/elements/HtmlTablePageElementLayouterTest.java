/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.generators.html.elements;

import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableCellPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TablePageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;
import org.junit.Test;

public class HtmlTablePageElementLayouterTest extends AbstractHtmlPageElementLayouterTest {

    @Test
    public void testLeereTabelle() {

        TablePageElement pageElement = new TablePageElement(true);

        HtmlTablePageElementLayouter elementLayouter = new HtmlTablePageElementLayouter(pageElement, layouter);

        elementLayouter.layout();

        String generateText = layouter.generateText();

        assertEquals(0, generateText.length());
    }

    @Test
    public void testTabelle() throws Exception {

        int rows = 5;
        int cols = 4;

        TablePageElement tablePageElement = new TablePageElement(true);

        for (int row = 0; row < rows; row++) {
            TableCellPageElement[] cellPageElements = new TableCellPageElement[cols];
            for (int col = 0; col < cols; col++) {
                cellPageElements[col] = new TableCellPageElement(new TextPageElement(createCellText(row, col)));
            }
            TableRowPageElement rowPageElement = new TableRowPageElement(cellPageElements);
            tablePageElement.addPageElements(rowPageElement);
        }

        HtmlTablePageElementLayouter elementLayouter = new HtmlTablePageElementLayouter(tablePageElement, layouter);

        elementLayouter.layout();

        String generateText = layouter.generateText();

        assertXpathExists(generateText, "/table");
        assertXpathExists(generateText, "/table[contains(@class, 'BORDER')]");

        for (int row = 0; row < rows; row++) {
            assertXpathExists(generateText, "/table/tr[" + (row + 1) + "]");
            for (int col = 0; col < cols; col++) {
                assertXpathExists(generateText, "/table/tr[" + (row + 1) + "]/td[" + (col + 1) + "][.='"
                        + createCellText(row, col) + "']");
            }
            assertXpathNotExists(generateText, "/table/tr[" + (row + 1) + "]/td[" + (cols + 2) + "]");
        }
        assertXpathNotExists(generateText, "/table/tr[" + (rows + 2) + "]");
    }

    protected String createCellText(int row, int col) {
        String cellText = row + "-" + col;
        return cellText;
    }
}
