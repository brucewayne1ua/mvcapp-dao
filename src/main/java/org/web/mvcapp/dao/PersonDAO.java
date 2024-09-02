package org.web.mvcapp.dao;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.web.mvcapp.models.Person;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PersonDAO {

    private final JdbcTemplate jdbcTemplate;

    public PersonDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Person> index() {
        return jdbcTemplate.query("SELECT * FROM Person", new BeanPropertyRowMapper<>(Person.class));
    }

    public Person show(int id) {
        return jdbcTemplate.query("SELECT * FROM person WHERE id = ?",
                        new Object[]{id},
                        new BeanPropertyRowMapper<>(Person.class))
                .stream().findAny().orElse(null);
    }

    public void save(Person person) {
        jdbcTemplate.update("INSERT INTO person (name, description, age, email) VALUES (?, ?, ?, ?)",
                person.getName(), person.getDescription(), person.getAge(), person.getEmail());
    }

    public void update(int id, Person updatedPerson) {
        jdbcTemplate.update("UPDATE person SET name = ?, description = ?, age = ?, email = ? WHERE id = ?",
                updatedPerson.getName(), updatedPerson.getDescription(), updatedPerson.getAge(),
                updatedPerson.getEmail(), id);
    }

    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM person WHERE id = ?", id);
    }

    public void testMultipleUpdate() {
        List<Person> people = create1000people();

        long before = System.currentTimeMillis();

        for (Person person : people) {
            jdbcTemplate.update("INSERT INTO person (name, description, age, email) VALUES (?, ?, ?, ?)",
                    person.getName(), person.getDescription(), person.getAge(), person.getEmail());
        }

        long after = System.currentTimeMillis();
        System.out.println("Time taken: " + (after - before));
    }

    public void testBatchUpdate() {
        List<Person> people = create1000people();

        long before = System.currentTimeMillis();

        jdbcTemplate.batchUpdate("INSERT INTO person (name, description, age, email) VALUES (?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Person person = people.get(i);
                        ps.setString(1, person.getName());
                        ps.setString(2, person.getDescription());
                        ps.setInt(3, person.getAge());
                        ps.setString(4, person.getEmail());
                    }

                    @Override
                    public int getBatchSize() {
                        return people.size();
                    }
                });

        long after = System.currentTimeMillis();
        System.out.println("Time taken: " + (after - before));
    }

    private List<Person> create1000people() {
        List<Person> people = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            people.add(new Person(i, "Name" + i, "Description" + i, 30, "email" + i + "@example.com"));
        }

        return people;
    }
}
