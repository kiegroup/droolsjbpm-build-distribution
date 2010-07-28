/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bitronix.tm.gui;

import bitronix.tm.journal.TransactionLogRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * <p></p>
 * <p>&copy; <a href="http://www.bitronix.be">Bitronix Software</a></p>
 *
 * @author lorban
 */
public class TransactionTableCellRenderer extends DefaultTableCellRenderer {

    public TransactionTableCellRenderer() {
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        TransactionLogRecord tlog = (TransactionLogRecord) ((RawTransactionTableModel)table.getModel()).getRow(row);
        if (!tlog.isCrc32Correct()) {
            if (isSelected)
                setBackground(table.getSelectionBackground());
            else
                component.setBackground(Color.RED);
        }
        else {
            if (isSelected)
                setBackground(table.getSelectionBackground());
            else
                component.setBackground(Color.WHITE);
        }
        return component;
    }

}
