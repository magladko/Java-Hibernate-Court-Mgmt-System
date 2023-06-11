package mas.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TrainingEquipment extends Equipment {

    @NonNull
    private String name;
    @NonNull
    private String description;

    public TrainingEquipment(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public TrainingEquipment(String notes, String name, String description) {
        super(notes);
        this.name = name;
        this.description = description;
    }

    @Override
    public String toString() {
        return "TrainingEquipment{" +
                "notes='" + getNotes() + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
