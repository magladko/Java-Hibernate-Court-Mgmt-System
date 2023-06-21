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

/**
 * Person entity class.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Person {

    /**
     * Enum for person types.
     */
    public enum PersonType {CLIENT, PARTICIPANT}

    /**
     * Id of the person.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    /**
     * Set of person types. It can hold one or two values, depending on the Person's type.
     * Client holds values CLIENT and PARTICIPANT.
     * Participant only holds value PARTICIPANT
     */
    @ElementCollection
    @Column(nullable = false)
    private Set<PersonType> personTypes = new HashSet<>();

    /**
     * Name of the person.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Surname of the person.
     */
    @Column(nullable = false)
    private String surname;

    /**
     * Phone number of the person. (null if not a Client)
     * Must not be null for Clients.
     */
    @Column(unique = true)
    private String phoneNr;

    /**
     * Email of the person. (null if not a Client)
     */
    @Column(unique = true)
    private String email;

    /**
     * Birthday of the person.
     */
    private LocalDate birthday;

    /**
     * Owning client of the person. (null if Client)
     */
    @ManyToOne
    private Person owningClient;

    /**
     * Sets the owning client of this Person object.
     *
     * @param owningClient The Person object representing the owning client.
     * @throws TypeMismatchException If the owningClient parameter is null and this Person object is a Participant without a Client,
     *                              or if the owningClient parameter is not a Client or if this Person object is not a Participant.
     */
    public void setOwningClient(Person owningClient) throws TypeMismatchException {
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


    /**
     * Set of owned participants. (only Clients can hold participants)
     */
    @OneToMany(mappedBy = "owningClient")
    private Set<Person> ownedParticipants = new HashSet<>();

    /**
     * Adds the specified participants to the list of owned participants for this Person object.
     *
     * @param ownedParticipants The array of Person objects representing the participants to be added.
     * @throws TypeMismatchException If any of the specified participants are not of type Participant,
     *                              or if this Person object is not of type Client.
     */
    public void addParticipants(Person... ownedParticipants) throws TypeMismatchException {
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


    /**
     * Adds the specified participants to the list of owned participants for this Person object.
     *
     * @param ownedParticipants The array of Person objects representing the participants to be added.
     * @throws TypeMismatchException If any of the specified participants are not of type Participant,
     *                              or if this Person object is not of type Client.
     * @see Person#addParticipants(Person...)
     */
    public void addOwnedParticipants(Person... ownedParticipants) throws TypeMismatchException {
        addParticipants(ownedParticipants);
    }

    /**
     * Removes the specified participants from the list of owned participants for this Person object.
     *
     * @param ownedParticipants The array of Person objects representing the participants to be removed.
     */
    public void removeParticipants(Person... ownedParticipants) {
        for (Person p : ownedParticipants) {
            if (this.getOwnedParticipants().contains(p)) {
                this.getOwnedParticipants().remove(p);
                p.setOwningClient(null);
            }
        }
    }

    /**
     * Removes the specified participants from the list of owned participants for this Person object.
     *
     * @param ownedParticipants The array of Person objects representing the participants to be removed.
     */
    public void removeOwnedParticipants(Person... ownedParticipants) {
        removeParticipants(ownedParticipants);
    }

    /**
     * Set of reservations bought by the client.
     */
    @OneToMany(mappedBy = "client")
    private Set<Reservation> reservationsBought = new HashSet<>();

    /**
     * Adds the specified reservations to the list of reservations bought by this Person object.
     *
     * @param reservationsBought The array of Reservation objects representing the reservations to be added.
     * @throws TypeMismatchException If this Person object is not of type Client,
     *                              or if any of the specified reservations do not belong to this Person.
     */
    public void addReservationsBought(Reservation... reservationsBought) throws TypeMismatchException {
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


    /**
     * Removes the specified reservations from the list of reservations bought by this Person object.
     *
     * @param reservationsBought The array of Reservation objects representing the reservations to be removed.
     * @throws TypeMismatchException If this Person object is not of type Client.
     */
    public void removeReservationsBought(Reservation... reservationsBought) throws TypeMismatchException {
        if (!this.getPersonTypes().contains(PersonType.CLIENT))
            throw new TypeMismatchException("This Person object is not a Client");

        for (Reservation r : reservationsBought) {
            if (this.getReservationsBought().contains(r)) {
                this.getReservationsBought().remove(r);
                r.setClient(null);
            }
        }
    }


    /**
     * Set of reservations for this participant.
     */
    @OneToMany(mappedBy = "participant")
    private Set<Reservation> reservations = new HashSet<>();

    /**
     * Adds the specified reservations to the list of reservations for this Person object.
     *
     * @param reservations The array of Reservation objects representing the reservations to be added.
     * @throws TypeMismatchException If any of the specified reservations do not belong to this Person.
     */
    public void addReservations(Reservation... reservations) throws TypeMismatchException {
        for (Reservation r : reservations) {
            if (!this.getReservations().contains(r)) {
                if (!r.getParticipant().equals(this))
                    throw new TypeMismatchException("Given Reservation does not belong to this Person");
                this.getReservations().add(r);
                r.setParticipant(this);
            }
        }
    }


    /**
     * Set of trainings for this participant.
     */
    @ManyToMany(mappedBy = "participants")
    private Set<Training> trainings = new HashSet<>();

    /**
     * Adds the specified trainings to the list of trainings for this Person object.
     *
     * @param trainings The array of Training objects representing the trainings to be added.
     * @throws TypeMismatchException If any of the specified trainings does not contain this Person as a participant.
     */
    public void addTrainings(Training... trainings) throws TypeMismatchException {
        for (Training t : trainings) {
            if (!this.getTrainings().contains(t)) {
                if (!t.getParticipants().contains(this))
                    throw new TypeMismatchException("Given Training does not contain this Person as Participant");
                this.getTrainings().add(t);
                t.addParticipants(this);
            }
        }
    }


    /**
     * Removes trainings.
     *
     * @param trainings The trainings to remove.
     */
    public void removeTrainings(Training... trainings) {
        for (Training t : trainings) {
            if (this.getTrainings().contains(t)) {
                this.getTrainings().remove(t);
                t.removeParticipants(this);
            }
        }
    }

    /**
     * Set of trainings bought by the client.
     */
    @ManyToMany(mappedBy = "clients")
    private Set<Training> trainingsBought = new HashSet<>();

    /**
     * Adds the specified trainings to the list of trainings bought by this Person object.
     *
     * @param trainingsBought The array of Training objects representing the trainings to be added.
     * @throws TypeMismatchException If this Person object is not of type Client or if any of the specified trainings
     *                              does not contain this Person as a client.
     */
    public void addTrainingsBought(Training... trainingsBought) throws TypeMismatchException {
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


    /**
     * Removes the specified trainings from the list of trainings bought by this Person object.
     *
     * @param trainingsBought The array of Training objects representing the trainings to be removed.
     * @throws TypeMismatchException If this Person object is not of type Client.
     */
    public void removeTrainingsBought(Training... trainingsBought) throws TypeMismatchException {
        if (!this.getPersonTypes().contains(PersonType.CLIENT))
            throw new TypeMismatchException("This Person object is not a Client");

        for (Training t : trainingsBought) {
            if (this.getTrainingsBought().contains(t)) {
                this.getTrainingsBought().remove(t);
                t.removeClients(this);
            }
        }
    }


    /**
     * Constructor for Person class.
     *
     * @param personTypes  The types of the person.
     * @param name         The name of the person.
     * @param surname      The surname of the person.
     * @param phoneNr      The phone number of the person.
     * @param email        The email address of the person.
     * @param birthday     The birthday of the person.
     * @param owningClient The owning client of the person.
     */
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
     * Registers a new client with given name, surname and phone number.
     *
     * @param name    The name of the client.
     * @param surname The surname of the client.
     * @param phoneNr The phone number of the client.
     * @param email   The email address of the client.
     * @return A new Person object with type CLIENT and PARTICIPANT and given parameters.
     */
    public static Person registerClient(String name, String surname, String phoneNr, String email) {
        return new Person(new PersonType[]{PersonType.CLIENT, PersonType.PARTICIPANT},
                name, surname, phoneNr, email, null, null);
    }

    /**
     * Registers a new client with given name, surname and phone number.
     *
     * @param name    The name of the client.
     * @param surname The surname of the client.
     * @param phoneNr The phone number of the client.
     * @return A new Person object with type CLIENT and PARTICIPANT and given parameters.
     */
    public static Person registerClient(String name, String surname, String phoneNr) {
        return registerClient(name, surname, phoneNr, null);
    }

    /**
     * Registers a new participant with given name and surname and birthday and owning client.
     *
     * @param name         The name of the participant.
     * @param surname      The surname of the participant.
     * @param birthday     The birthday of the participant.
     * @param owningClient The owning client of the participant.
     * @return A new Person object with type PARTICIPANT and given parameters.
     */
    public static Person registerParticipant(String name, String surname, LocalDate birthday, Person owningClient) {
        return new Person(new PersonType[]{PersonType.PARTICIPANT}, name, surname,
                null, null, birthday, owningClient);
    }

    /**
     * Registers a new participant with given name and surname and owning client.
     *
     * @param name         The name of the participant.
     * @param surname      The surname of the participant.
     * @param owningClient The owning client of the participant.
     * @return A new Person object with type PARTICIPANT and given parameters.
     */
    public static Person registerParticipant(String name, String surname, Person owningClient) {
        return registerParticipant(name, surname, null, owningClient);
    }

    /**
     * Registers a person as a client with given phone number and email address. If already registered as a client,
     * returns true without changing anything. If not registered as a participant or not owned by any clients,
     * throws TypeMismatchException. Otherwise, sets phone number and email address to given values and sets
     * owning client to null.
     * <p>
     * Returns true if successful.
     * <p>
     * Throws TypeMismatchException if not successful.
     *
     * @param phoneNr Phone number to set for this person
     * @param email   Email address to set for this person
     * @return true if successful
     * @throws TypeMismatchException if not successful
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
     * Registers a person as a participant.
     *
     * @param birthday the birthday of the person
     * @return true if the person is registered as a participant, false otherwise
     * @throws TypeMismatchException if the person is not a client
     */
    public boolean registerAsParticipant(LocalDate birthday) throws TypeMismatchException {
        if (getPersonTypes().contains(PersonType.PARTICIPANT)) return true;
        if (!getPersonTypes().contains(PersonType.CLIENT))
            throw new TypeMismatchException("Person is not a client.");
        getPersonTypes().add(PersonType.PARTICIPANT);
        setBirthday(birthday);
        return true;
    }

    /**
     * Registers a person as a participant.
     *
     * @return true if the person is registered as a participant, false otherwise
     * @throws TypeMismatchException if the person is not a client
     */
    public boolean registerAsParticipant() throws TypeMismatchException {
        return registerAsParticipant(null);
    }

    /**
     * Returns a string representation of this Person object.
     *
     * @return A string representation of the Person object.
     * @throws TypeMismatchException If the Person object is not a Client or Participant.
     */
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

    /**
     * Checks if this Person object is equal to the specified object.
     *
     * @param o The object to compare to this Person object.
     * @return {@code true} if the objects are equal, {@code false} otherwise.
     * @throws TypeMismatchException If the Person object is not a Client nor Participant.
     */
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

    /**
     * Returns the hash code value for this Person object.
     *
     * @return The hash code value for this Person object.
     */
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
