package mas.util;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;

import java.util.ArrayList;

/**
 * This class represents a weak adapter for managing change listeners in a Tennis Courts management application.
 * It allows adding and removing various types of change listeners, such as ChangeListener, InvalidationListener,
 * ListChangeListener, and binding StringProperty and BooleanProperty to corresponding expressions.
 */
public class WeakAdapter {

    private final ArrayList<Object> listenerRefs = new ArrayList<>();

    /**
     * Constructs a new WeakAdapter.
     */
    public WeakAdapter() {}

    /**
     * Clears all the registered listeners.
     */
    public void dispose() {
        listenerRefs.clear();
    }

    /**
     * Removes the specified ChangeListener from the registered listeners.
     *
     * @param listener the ChangeListener to remove
     * @param <T> the type of the value being observed
     */
    public final <T> void remove(ChangeListener<T> listener) {
        listenerRefs.remove(listener);
    }

    /**
     * Adds a ChangeListener to the given ObservableValue with weak referencing.
     * The listener will be automatically removed when it becomes garbage collected.
     *
     * @param observable the ObservableValue to listen to
     * @param listener the ChangeListener to add
     * @param <T> the type of the value being observed
     */
    public final <T> void addChangeListener(final ObservableValue<T> observable, ChangeListener<T> listener) {
        listenerRefs.add(listener);
        observable.addListener(new WeakChangeListener<>(listener));
    }

    /**
     * Adds a ListChangeListener with weak referencing.
     * The listener will be automatically removed when it becomes garbage collected.
     *
     * @param listener the ListChangeListener to add
     * @param <T> the type of the elements in the list
     * @return the created WeakListChangeListener
     */
    public final <T> WeakListChangeListener<T> addListChangeListener(ListChangeListener<T> listener) {
        listenerRefs.add(listener);
        return new WeakListChangeListener<>(listener);
    }

    /**
     * Adds an InvalidationListener to the given Observable with weak referencing.
     * The listener will be automatically removed when it becomes garbage collected.
     *
     * @param observed the Observable to listen to
     * @param listener the InvalidationListener to add
     */
    public void addInvalidationListener(final Observable observed, InvalidationListener listener) {
        listenerRefs.add(listener);
        observed.addListener(new WeakInvalidationListener(listener));
    }

    /**
     * Binds a StringProperty to a StringExpression with weak referencing.
     * The StringProperty will be automatically updated when the StringExpression changes.
     *
     * @param propertyToUpdate the StringProperty to update
     * @param expressionToListen the StringExpression to listen to
     */
    public final void stringBind(final StringProperty propertyToUpdate, final StringExpression expressionToListen) {
        ChangeListener<String> listener = (ov, oldValue, newValue) -> propertyToUpdate.set(newValue);
        listenerRefs.add(listener);
        expressionToListen.addListener(new WeakChangeListener<>(listener));
        listener.changed(null, null, expressionToListen.get());
    }

    /**
     * Binds a BooleanProperty to a BooleanExpression with weak referencing.
     * The BooleanProperty will be automatically updated when the BooleanExpression changes.
     *
     * @param propertyToUpdate the BooleanProperty to update
     * @param expressionToListen the BooleanExpression to listen to
     */
    public final void booleanBind(final BooleanProperty propertyToUpdate, final BooleanExpression expressionToListen) {
        ChangeListener<Boolean> listener = (ov, oldValue, newValue) -> propertyToUpdate.set(newValue);
        listenerRefs.add(listener);
        expressionToListen.addListener(new WeakChangeListener<>(listener));
        propertyToUpdate.set(expressionToListen.get());
    }
}

