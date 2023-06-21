package mas.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Represents a training equipment used in a tennis court management application.
 * It extends the Equipment class.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class TrainingEquipment extends Equipment {

    private String name;
    private String description;

    /**
     * Constructs a TrainingEquipment object with the given name and description.
     *
     * @param name        the name of the training equipment
     * @param description the description of the training equipment
     */
    public TrainingEquipment(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Constructs a TrainingEquipment object with the given notes, name, and description.
     *
     * @param notes       the notes for the training equipment
     * @param name        the name of the training equipment
     * @param description the description of the training equipment
     */
    public TrainingEquipment(String notes, String name, String description) {
        super(notes);
        this.name = name;
        this.description = description;
    }

    /**
     * Returns a string representation of the TrainingEquipment object.
     *
     * @return a string representation of the TrainingEquipment object
     */
    @Override
    public String toString() {
        return "TrainingEquipment{" +
                "notes='" + getNotes() + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o the reference object with which to compare
     * @return true if this object is the same as the o argument; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrainingEquipment that)) return false;
        if (!super.equals(o)) return false;

        if (!getName().equals(that.getName())) return false;
        return getDescription() != null ? getDescription().equals(that.getDescription()) : that.getDescription() == null;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        return result;
    }
}
