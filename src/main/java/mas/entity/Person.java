package mas.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Null;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Person {

    public enum PersonType { Client, Participant }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @ElementCollection
    @Column(nullable = false)
    private Set<PersonType> personTypes = new HashSet<>();

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Column(unique = true)
    private String phoneNr;

    @Column(unique = true)
    private String email;

    private LocalDate birthday;

    @ManyToOne
    private Person owningClient;

    @OneToMany(mappedBy = "owningClient")
    private Set<Person> ownedParticipants = new HashSet<>();



}
