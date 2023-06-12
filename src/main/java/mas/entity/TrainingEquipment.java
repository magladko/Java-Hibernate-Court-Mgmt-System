package mas.entity;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TrainingEquipment extends Equipment {

    private String name;
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
