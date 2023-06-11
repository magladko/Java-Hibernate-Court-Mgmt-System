package mas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Racket extends Equipment {

    @Column(nullable = false)
    private String manufacturer;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private BigDecimal pricePerHour;

}
