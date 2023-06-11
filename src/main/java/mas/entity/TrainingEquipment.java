package mas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TrainingEquipment extends Equipment {

    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
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
}
