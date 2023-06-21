package mas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    public enum PersonType {CLIENT, PARTICIPANT}

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

    public void setOwningClient(Person owningClient) {
        if (owningClient == this.getOwningClient()) return;

        if (owningClient == null) {
            if (!this.getPersonTypes().contains(PersonType.CLIENT) && this.getPersonTypes().contains(PersonType.PARTICIPANT))
                throw new TypeMismatchException("This Person object is only a Participant and must have a Client");
            this.getOwningClient().removeParticipants(this);
            this.owningClient = null;
        } else {
            if (!owningClient.getPersonTypes().contains(PersonType.CLIENT))
                throw new TypeMismatchException("Given Person is not a Client");
            if (!this.getPersonTypes().contains(PersonType.PARTICIPANT))
                throw new TypeMismatchException("This Person object is not a Participant");

            this.owningClient = owningClient;
            getOwningClient().addOwnedParticipants(this);
        }
    }

    @OneToMany(mappedBy = "owningClient")
    private Set<Person> ownedParticipants = new HashSet<>();

    public void addParticipants(Person... ownedParticipants) {
        for (Person p : ownedParticipants) {
            if (!this.getOwnedParticipants().contains(p)) {
                if (!p.getPersonTypes().contains(PersonType.PARTICIPANT))
                    throw new TypeMismatchException("Given Person is not a Participant");
                if (!this.getPersonTypes().contains(PersonType.CLIENT))
                    throw new TypeMismatchException("This Person object is not a Client");
                this.getOwnedParticipants().add(p);
                p.setOwningClient(this);
            }
        }
    }

    public void addOwnedParticipants(Person... ownedParticipants) {
        addParticipants(ownedParticipants);
    }

    public void removeParticipants(Person... ownedParticipants) {
        for (Person p : ownedParticipants) {
            if (this.getOwnedParticipants().contains(p)) {
                this.getOwnedParticipants().remove(p);
                p.setOwningClient(null);
            }
        }
    }

    public void removeOwnedParticipants(Person... ownedParticipants) {
        removeParticipants(ownedParticipants);
    }

    @OneToMany(mappedBy = "client")
    private Set<Reservation> reservationsBought = new HashSet<>();

    public void addReservationsBought(Reservation... reservationsBought) {
        if (!this.getPersonTypes().contains(PersonType.CLIENT))
            throw new TypeMismatchException("This Person object is not a Client");

        for (Reservation r : reservationsBought) {
            if (!this.getReservationsBought().contains(r)) {
                if (!r.getClient().equals(this))
                    throw new TypeMismatchException("Given Reservation does not belong to this Person");
                this.getReservationsBought().add(r);
                r.setClient(this);
            }
        }
    }

    public void removeReservationsBought(Reservation... reservationsBought) {
        if (!this.getPersonTypes().contains(PersonType.CLIENT))
            throw new TypeMismatchException("This Person object is not a Client");

        for (Reservation r : reservationsBought) {
            if (this.getReservationsBought().contains(r)) {
                this.getReservationsBought().remove(r);
                r.setClient(null);
            }
        }
    }

    @OneToMany(mappedBy = "participant")
    private Set<Reservation> reservations = new HashSet<>();

    public void addReservations(Reservation... reservations) {
        for (Reservation r : reservations) {
            if (!this.getReservations().contains(r)) {
                if (!r.getParticipant().equals(this))
                    throw new TypeMismatchException("Given Reservation does not belong to this Person");
                this.getReservations().add(r);
                r.setParticipant(this);
            }
        }
    }

    @ManyToMany(mappedBy = "participants")
    private Set<Training> trainings = new HashSet<>();

    public void addTrainings(Training... trainings) {
        for (Training t : trainings) {
            if (!this.getTrainings().contains(t)) {
                if (!t.getParticipants().contains(this))
                    throw new TypeMismatchException("Given Training does not contain this Person as Participant");
                this.getTrainings().add(t);
                t.addParticipants(this);
            }
        }
    }

    public void removeTrainings(Training... trainings) {
        for (Training t : trainings) {
            if (this.getTrainings().contains(t)) {
                this.getTrainings().remove(t);
                t.removeParticipants(this);
            }
        }
    }

    @ManyToMany(mappedBy = "clients")
    private Set<Training> trainingsBought = new HashSet<>();

    public void addTrainingsBought(Training... trainingsBought) {
        if (!this.getPersonTypes().contains(PersonType.CLIENT))
            throw new TypeMismatchException("This Person object is not a Client");

        for (Training t : trainingsBought) {
            if (!this.getTrainingsBought().contains(t)) {
                if (!t.getClients().contains(this))
                    throw new TypeMismatchException("Given Training does not contain this Person as Client");
                this.getTrainingsBought().add(t);
                t.addClients(this);
            }
        }
    }

    public void removeTrainingsBought(Training... trainingsBought) {
        if (!this.getPersonTypes().contains(PersonType.CLIENT))
            throw new TypeMismatchException("This Person object is not a Client");

        for (Training t : trainingsBought) {
            if (this.getTrainingsBought().contains(t)) {
                this.getTrainingsBought().remove(t);
                t.removeClients(this);
            }
        }
    }

    private Person(PersonType[] personTypes, String name, String surname, String phoneNr, String email,
                   LocalDate birthday, Person owningClient) {
        this.getPersonTypes().addAll(Arrays.asList(personTypes));
        this.name = name;
        this.surname = surname;
        this.phoneNr = phoneNr;
        this.email = email;
        this.birthday = birthday;
        this.setOwningClient(owningClient);
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
        return new Person(new PersonType[]{ PersonType.CLIENT, PersonType.PARTICIPANT},
                          name, surname, phoneNr, email, null, null);
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
        return new Person(new PersonType[]{ PersonType.PARTICIPANT}, name, surname, null, null, birthday, client);
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
        if (getPersonTypes().contains(PersonType.CLIENT)) return true;
        if (!getPersonTypes().contains(PersonType.PARTICIPANT))
            throw new TypeMismatchException("Person is not a participant.");
        getPersonTypes().add(PersonType.CLIENT);
        setPhoneNr(phoneNr);
        setEmail(email);

        setOwningClient(null);

        return true;
    }

    /**
     * Extend existing Client as new Participant (and Client).
     * @param birthday birthday
     * @return True if Person is registered as Client (or has already been one), false otherwise.
     */
    public boolean registerAsParticipant(LocalDate birthday) {
        if (getPersonTypes().contains(PersonType.PARTICIPANT)) return true;
        if (!getPersonTypes().contains(PersonType.CLIENT))
            throw new TypeMismatchException("Person is not a client.");
        getPersonTypes().add(PersonType.PARTICIPANT);
        setBirthday(birthday);
        return true;
    }

    /**
     * Extend existing Client as new Participant (and Client).
     * @see Person#registerAsParticipant(LocalDate)
     * @return True if Person is registered as Client (or has already been one), false otherwise.
     */
    public boolean registerAsParticipant() {
        return registerAsParticipant(null);
    }

    @Override
    public String toString() {
        if (getPersonTypes().contains(PersonType.CLIENT) && getPersonTypes().contains(PersonType.PARTICIPANT)) {
            return "Client(Participant){" +
                    "name='" + name + '\'' +
                    ", surname='" + surname + '\'' +
                    ", phoneNr='" + phoneNr + '\'' +
                    ", email='" + email + '\'' +
                    ", birthday=" + birthday +
                    ", ownedParticipants=" + ownedParticipants.stream().map(p -> p.getName() + " " + p.getSurname()).toList() +
                    '}';
        } else if (getPersonTypes().contains(PersonType.PARTICIPANT)) {
            return "Participant{" +
                    "name='" + name + '\'' +
                    ", surname='" + surname + '\'' +
                    ", birthday=" + birthday +
                    ", owningClient=" + owningClient.getId() +
                    '}';
        }
        throw new TypeMismatchException("Person is not a Client or Participant");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person person)) return false;

        if (!getPersonTypes().equals(person.getPersonTypes())) return false;
        if (getPersonTypes().contains(PersonType.CLIENT)) {
            // Client
            if (!getPhoneNr().equals(person.getPhoneNr())) return false;
        }

        if (getPersonTypes().contains(PersonType.PARTICIPANT)) {
            // Participant
            if (!getName().equals(person.getName())) return false;
            if (!getSurname().equals(person.getSurname())) return false;
            return getOwningClient() != null ? getOwningClient().equals(person.getOwningClient()) : person.getOwningClient() == null;
        }

        throw new TypeMismatchException("Person is not a Client nor Participant");
    }

    @Override
    public int hashCode() {
        int result = getPersonTypes().hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + getSurname().hashCode();
        result = 31 * result + (getPhoneNr() != null ? getPhoneNr().hashCode() : 0);
        result = 31 * result + (getOwningClient() != null ? getOwningClient().hashCode() : 0);
        return result;
    }
}
