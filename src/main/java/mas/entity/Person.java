package mas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.TypeMismatchException;

import java.time.LocalDate;
import java.util.Arrays;
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

    public boolean changeOwningClient(@NonNull Person newOwningClient) {
        if (!newOwningClient.getPersonTypes().contains(PersonType.Client)) return false;
        this.owningClient.getOwnedParticipants().remove(this);
        this.owningClient = newOwningClient;
        newOwningClient.getOwnedParticipants().add(this);
        return true;
    }

    @OneToMany(mappedBy = "owningClient")
    private Set<Person> ownedParticipants = new HashSet<>();

    public void addParticipant(@NonNull Person participant) {
        if (!participant.getPersonTypes().contains(PersonType.Participant))
            throw new TypeMismatchException("Given Person is not a Participant");
        if (!getPersonTypes().contains(PersonType.Client))
            throw new TypeMismatchException("This Person object is not a Client");

        getOwnedParticipants().add(participant);
        participant.setOwningClient(this);
    }

    @OneToMany(mappedBy = "client")
    private Set<Reservation> reservationsBought = new HashSet<>();

    @OneToMany(mappedBy = "participant")
    private Set<Reservation> reservations = new HashSet<>();

    @ManyToMany(mappedBy = "participants")
    private Set<Training> trainings = new HashSet<>();

    @ManyToMany(mappedBy = "clients")
    private Set<Training> trainingsBought = new HashSet<>();

    private Person(PersonType[] personTypes, String name, String surname, String phoneNr, String email,
                   LocalDate birthday) {
        this.getPersonTypes().addAll(Arrays.asList(personTypes));
        this.name = name;
        this.surname = surname;
        this.phoneNr = phoneNr;
        this.email = email;
        this.birthday = birthday;
    }

    /**
     * Register Person as Client (and Participant).
     * @param name name
     * @param surname surname
     * @param phoneNr phone number
     * @param email email
     * @return Newly registered client.
     */
    public static Person registerClient(String name, String surname, String phoneNr, String email) {
        return new Person(new PersonType[]{ PersonType.Client, PersonType.Participant },
                          name, surname, phoneNr, email, null);
    }

    /**
     * Register Person as Client (and Participant).
     * @param name name
     * @param surname surname
     * @param phoneNr phone number
     * @return Newly registered client.
     */
    public static Person registerClient(String name, String surname, String phoneNr) {
        return registerClient(name, surname, phoneNr, null);
    }

    /**
     * Register person as Participant only.
     * @param birthday birthday
     * @param name name
     * @param surname surname
     * @return Newly registered participant.
     */
    public static Person registerParticipant(String name, String surname, LocalDate birthday, Person client) {
        Person participant = new Person(new PersonType[]{ PersonType.Participant }, name, surname, null, null, birthday);
        client.addParticipant(participant);
        return participant;
    }

    /**
     * Register person as Participant only.
     * @param name name
     * @param surname surname
     * @param client client
     * @return Newly registered participant.
     */
    public static Person registerParticipant(String name, String surname, Person client) {
        return registerParticipant(name, surname, null, client);
    }

    /**
     * Register existing Participant as new Client (and Participant).
     * This will remove existing object as owned participant.
     * @param email email
     * @param phoneNr phone number
     * @return True if Person is registered as Client (or has already been one), false otherwise.
     * @throws TypeMismatchException Exception thrown if the Person is not a Participant.
     */
    public boolean registerAsClient(String phoneNr, String email) throws TypeMismatchException {
        if (getPersonTypes().contains(PersonType.Client)) return true;
        if (!getPersonTypes().contains(PersonType.Participant))
            throw new TypeMismatchException("Person is not a participant.");
        getPersonTypes().add(PersonType.Client);
        setPhoneNr(phoneNr);
        setEmail(email);

        getOwningClient().getOwnedParticipants().remove(this);
        setOwningClient(null);

        return true;
    }

    /**
     * Extend existing Client as new Participant (and Client).
     * @param birthday birthday
     * @return True if Person is registered as Client (or has already been one), false otherwise.
     */
    public boolean registerAsParticipant(LocalDate birthday) {
        if (getPersonTypes().contains(PersonType.Participant)) return true;
        if (!getPersonTypes().contains(PersonType.Client))
            throw new TypeMismatchException("Person is not a client.");
        getPersonTypes().add(PersonType.Participant);
        setBirthday(birthday);
        return true;
    }

    /**
     * Register Person as Client (and Participant).
     * @return True if Person is registered as Client (or has already been one), false otherwise.
     */
    public boolean registerAsParticipant() {
        return registerAsParticipant(null);
    }

    @Override
    public String toString() {
        if (getPersonTypes().contains(PersonType.Client) && getPersonTypes().contains(PersonType.Participant)) {
            return "Client(Participant){" +
                    ", name='" + name + '\'' +
                    ", surname='" + surname + '\'' +
                    ", phoneNr='" + phoneNr + '\'' +
                    ", email='" + email + '\'' +
                    ", birthday=" + birthday +
                    ", ownedParticipants=" + ownedParticipants +
                    '}';
        } else if (getPersonTypes().contains(PersonType.Participant)) {
            return "Participant{" +
                    ", name='" + name + '\'' +
                    ", surname='" + surname + '\'' +
                    ", birthday=" + birthday +
                    ", owningClient=" + owningClient.getId() +
                    '}';
        }
        throw new TypeMismatchException("Person is not a Client or Participant");
    }
}
