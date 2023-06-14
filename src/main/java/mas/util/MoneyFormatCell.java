package mas.util;

import javafx.scene.control.TableCell;
import mas.entity.Court;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class MoneyFormatCell extends TableCell<Court, BigDecimal> {
    @Override
    protected void updateItem(BigDecimal item, boolean empty) {
        super.updateItem(item, empty);
        setText(item == null ? "" : NumberFormat.getCurrencyInstance().format(item));
    }
}
