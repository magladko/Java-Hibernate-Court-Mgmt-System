package mas.util;

import javafx.scene.control.TableCell;
import mas.entity.Court;

import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 * This class is a custom TableCell for displaying BigDecimal values as formatted currency in a TableView.
 * It is used in the Tennis Courts management application to format money values in the UI.
 *
 */
public class MoneyFormatCell extends TableCell<Court, BigDecimal> {

    /**
     * Updates the item value of the TableCell and displays it as formatted currency.
     *
     * @param item  the BigDecimal item to display
     * @param empty a boolean indicating if the cell is empty or not
     */
    @Override
    protected void updateItem(BigDecimal item, boolean empty) {
        super.updateItem(item, empty);
        setText(item == null ? "" : NumberFormat.getCurrencyInstance().format(item));
    }
}
